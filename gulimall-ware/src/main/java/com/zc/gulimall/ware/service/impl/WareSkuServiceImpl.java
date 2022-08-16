package com.zc.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.zc.common.enume.OrderStatusEnum;
import com.zc.common.exception.NoStockException;
import com.zc.common.to.SkuHasStockVO;
import com.zc.common.to.mq.OrderTo;
import com.zc.common.to.mq.StockDetailTo;
import com.zc.common.to.mq.StockLockedTo;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;
import com.zc.common.utils.R;
import com.zc.gulimall.ware.dao.WareSkuDao;
import com.zc.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.zc.gulimall.ware.entity.WareOrderTaskEntity;
import com.zc.gulimall.ware.entity.WareSkuEntity;
import com.zc.gulimall.ware.entity.vo.OrderItemVo;
import com.zc.gulimall.ware.entity.vo.OrderVo;
import com.zc.gulimall.ware.entity.vo.SkuWareHasStock;
import com.zc.gulimall.ware.entity.vo.WareSkuLockVo;
import com.zc.gulimall.ware.feign.OrderFeignService;
import com.zc.gulimall.ware.feign.ProductFeignService;
import com.zc.gulimall.ware.service.WareOrderTaskDetailService;
import com.zc.gulimall.ware.service.WareOrderTaskService;
import com.zc.gulimall.ware.service.WareSkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private WareOrderTaskService wareOrderTaskService;
    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    private OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<WareSkuEntity>();
        String skuId = (String) params.get("skuId");
        if(StringUtils.isNotEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        //仓库id
        String wareId = (String) params.get("wareId");
        if(StringUtils.isNotEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据skuId和wareId查询
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryByCondition(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);

    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities != null && wareSkuEntities.size() > 0) {
            // 如果不是空的,添加库存
            baseMapper.addStock(skuId, wareId, skuNum);
        } else {
            // 保存新的
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            // 远程查询sku名字
            String skuName = productFeignService.getSkuName(skuId);
            wareSkuEntity.setSkuName(skuName);
            baseMapper.insert(wareSkuEntity);
        }
    }

    @Override
    public List<SkuHasStockVO> getSkuHasStock(List<Long> skuIds) {

        List<SkuHasStockVO> hasStockVOS = skuIds.stream().map(skuId -> {
            SkuHasStockVO vo = new SkuHasStockVO();

            //查询当前sku的总库存量
            //select sum(stock-stock_locked) from `wms_ware` where sku_id=?
            Long count = baseMapper.getSkuStock(skuId);

            vo.setSkuId(skuId);
            vo.setHasStock(count==null?false:count>0);
            return vo;
        }).collect(Collectors.toList());
        return hasStockVOS;
    }

    /**
     * 为某个订单锁定库存
     * @param vo
     * @return
     *
     * 库存解锁的场景
     * 1）、下订单成功，订单过期没有支付被系统自动取消、被用户手动取消。都要解锁库存
     *
     * 2）、下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。
     *      之前锁定的库存就要自动解锁。
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存库存工单单的详情
         * 追溯。
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        //1、按照下单的收货地址，找到一个就近仓库，锁定库存。
        //1、找到每个商品在哪个仓库豆油库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询该商品在哪里有库存
            List<Long> wareIds = this.baseMapper.listWareIdHasSkuStock(skuId);
            stock.setWareIds(wareIds);
            return stock;
        }).collect(Collectors.toList());

        //2、锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareIds();
            if(CollectionUtils.isEmpty(wareIds)) {
                //没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            //1、如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发送给MQ
            //2、锁定失败。前面保存的工作单信息就回滚了。发送出去的消息即使要解锁记录，由于去数据库查不到id，所以不用解锁。
            //
            for (Long wareId : wareIds) {
                //成功返回1，否则是0
                Long count = this.baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                if(count > 0) {
                    skuStocked = true;
                    //TODO 告诉MQ库存锁定成功
                    String skuName = productFeignService.getSkuName(skuId);
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, skuName, hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(entity);

                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity, stockDetailTo);
                    //只发id不行，防止回滚以后找不到数据
                    stockLockedTo.setDetailTo(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                }
            }
            if(skuStocked == false) {
                //当前商品所有库存都没有锁住
                throw new NoStockException(skuId);
            }
        }

        //3、肯定全部都是锁定成功的
        return true;
    }

    /**
     * 解锁库存
     * @param to
     */
    @Override
    public void unlockStock(StockLockedTo to) {
        StockDetailTo detail = to.getDetailTo();
        Long detailId = detail.getId();
        //解锁
        //1、查询数据库关于这个订单的锁定库存信息
        //  有：证明库存锁定成功了
        //      解锁：订单情况。
        //          1、没有这个订单。必须解锁
        //          2、有这个订单。不是解锁库存。
        //                  订单撞他：已取消，解锁库存
        //                          未取消，不能解锁
        //  没有：库存锁定失败了，库存回滚了。这种情况无需解锁
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if(byId != null){
            //解锁
            Long id = to.getId();//库存工作单id
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();//根据订单号查询订单的状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if(r.getCode() == 0) {
                //订单数据返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });

                if(data == null || data.getStatus() == OrderStatusEnum.CANCLED.getCode()){
                    //订单已取消。解锁库存
                    if(byId.getLockStatus() == 1) {
                        //当前库存工作单详情，状态1已锁定但是未解锁才可以解锁
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                throw new RuntimeException("远程服务失败");
            }

        } else {
            //无需解锁
        }
    }

    /**
     * 防止订单服务卡顿，导致订单状态消息一直无法改变，库存消息优先到期。查订单状态是新建状态，什么都不做就走了。
     * 导致卡顿的订单永远无法解锁库存
     * @param orderTo
     */
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
       String orderSn = orderTo.getOrderSn();
       //查一下最新库存的状态，防止重复解锁
        WareOrderTaskEntity task = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        //按照工作单找到所有没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id)
                .eq("lock_status", 1));
        //Long skuId, Long wareId, Integer num, Long taskDetailId
        for (WareOrderTaskDetailEntity entity : entities) {
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }

    /**
     * 解锁库存
     * @param skuId
     * @param wareId
     * @param num
     * @param taskDetailId
     */
    @Transactional
    public void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        //库存解锁
        this.baseMapper.unlockStock(skuId, wareId, num);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);//变为已解锁
        wareOrderTaskDetailService.updateById(entity);
    }
}