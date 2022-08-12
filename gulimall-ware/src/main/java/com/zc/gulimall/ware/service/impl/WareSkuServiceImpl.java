package com.zc.gulimall.ware.service.impl;

import com.zc.common.exception.NoStockException;
import com.zc.common.to.SkuHasStockVO;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;
import com.zc.gulimall.ware.dao.WareSkuDao;
import com.zc.gulimall.ware.entity.WareSkuEntity;
import com.zc.gulimall.ware.entity.vo.OrderItemVo;
import com.zc.gulimall.ware.entity.vo.SkuWareHasStock;
import com.zc.gulimall.ware.entity.vo.WareSkuLockVo;
import com.zc.gulimall.ware.feign.ProductFeignService;
import com.zc.gulimall.ware.service.WareSkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

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
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
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
            for (Long wareId : wareIds) {

                //成功返回1，否则是0
                Long count = this.baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                if(count > 0) {
                    skuStocked = true;
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
}