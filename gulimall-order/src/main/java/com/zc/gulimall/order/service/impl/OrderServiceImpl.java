package com.zc.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zc.common.exception.NoStockException;
import com.zc.common.to.mq.OrderTo;
import com.zc.common.utils.R;
import com.zc.common.vo.MemberRespVo;
import com.zc.gulimall.order.config.AlipayTemplate;
import com.zc.gulimall.order.constant.OrderConstant;
import com.zc.gulimall.order.entity.OrderItemEntity;
import com.zc.gulimall.order.entity.PaymentInfoEntity;
import com.zc.gulimall.order.entity.to.OrderCreateTo;
import com.zc.gulimall.order.entity.vo.*;
import com.zc.common.enume.OrderStatusEnum;
import com.zc.gulimall.order.feign.CartFeignService;
import com.zc.gulimall.order.feign.MemberFeignService;
import com.zc.gulimall.order.feign.ProductFeignService;
import com.zc.gulimall.order.feign.WmsFeignService;
import com.zc.gulimall.order.interceptor.LoginUserInterceptor;
import com.zc.gulimall.order.service.OrderItemService;
import com.zc.gulimall.order.service.PaymentInfoService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;

import com.zc.gulimall.order.dao.OrderDao;
import com.zc.gulimall.order.entity.OrderEntity;
import com.zc.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> submitThreadLocal = new ThreadLocal<>();
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private WmsFeignService wmsFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private PaymentInfoService paymentInfoService;
    @Autowired
    private AlipayTemplate alipayTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单确认页返回需要用的数据
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        //获取之前的请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //线程共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //1、远程查询所有的收货地址列表
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            orderConfirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            //线程共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //2、远程查询购物车所有选中的购物项
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());

            R r = wmsFeignService.getSkuHasStock(collect);
            List<SkuStockVo> data = r.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if(!CollectionUtils.isEmpty(data)) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                orderConfirmVo.setStocks(map);
            }
        }, executor);

        //feign在远程调用之前要构造请求，调用很多的拦截器
        //RequestInterceptor interceptor : requestInterceptors

        //3、查询用户积分
        Integer integration = memberRespVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        //4、其他数据自动计算

        //TODO 5、防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).join();

        return orderConfirmVo;
    }

    /**
     * 下单
     * @param vo
     * @return
     */
    //本地事务：在分布式系统，只能控制住自己的回滚，控制不了其他服务的回滚
    //分布式事务：最大原因。网络问题+分布式机器。
//    @GlobalTransactional  //高并发
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        responseVo.setCode(0);
        submitThreadLocal.set(vo);
        //1、验证令牌【令牌的对比和删除必须保证原子性】
        //0【令牌失败】 - 1【删除成功】
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        //原子验证令牌和删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if(result == 0L) {
            //令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            //令牌验证成功
            //下单：去创建订单，验证令牌，验证价格，锁定库存......
            //1、创建订单、订单项等信息
            OrderCreateTo order = createOrder();
            //2、验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if(Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //金额对比成功
                //TODO 3、保存订单
                saveOrder(order);
                //4、锁定库存。只要有异常进行回滚订单数据
                //订单号，所有订单项（skuId，skuName，num）
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                //TODO 4、远程锁库存

                //为了保证高并发。库存服务自己回滚。可以发消息给库存服务；
                //库存服务本身也可以使用自动解锁模式  消息队列
                R r = wmsFeignService.orderLockStock(lockVo);
                if(r.getCode() == 0) {
                    //锁定成功
                    responseVo.setOrder(order.getOrder());

//                    int i = 10/0;//订单回滚，库存不滚
                    //TODO 订单创建成功发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return responseVo;
                } else {
                    //锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }

            } else {
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    /**
     * 根据订单号查询订单信息
     * @param orderSn
     * @return
     */
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    /**
     * 关闭订单
     * @param entity
     */
    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前这个订单的最新状态
        OrderEntity orderEntity = getById(entity.getId());

        if(entity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            //关单
            OrderEntity updateOrder = new OrderEntity();
            updateOrder.setId(entity.getId());
            updateOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
            updateById(updateOrder);

            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            //发给MQ一个
            try {
                //TODO 保证消息一定会发送出去，每一个消息都可以做好日志记录（给数据库保存每一个消息的详细信息）。
                //TODO 定期扫描数据库将失败的消息再发送一遍
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                //TODO 将没发送成功的消息进行重试发送。
            }

        }
    }

    /**
     * 获取当前订单的支付信息
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = getOrderByOrderSn(orderSn);

        BigDecimal decimal = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(decimal.toString());//支付金额
        payVo.setOut_trade_no(order.getOrderSn());//订单号

        OrderItemEntity itemEntity = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn)).get(0);
        payVo.setSubject(itemEntity.getSkuName());//订单主题
        payVo.setBody(itemEntity.getSkuAttrsVals());//订单备注

        return payVo;
    }

    /**
     * 分页查询当前登录用户的所有订单
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", respVo.getId())
                        .orderByDesc("id")
        );

        List<OrderEntity> order_sn = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(orderItemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(order_sn);

        return new PageUtils(page);
    }

    /**
     * 处理支付宝的支付结果
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //1、保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());
        infoEntity.setSubject(vo.getSubject());

        paymentInfoService.save(infoEntity);

        //2、修改订单的状态信息
        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {
            //支付成功
            this.baseMapper.updateOrderStatus(vo.getOut_trade_no(), OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    /**
     * 支付宝验签
     * @param request
     * @return
     */
    @Override
    public boolean rasCheckV1(HttpServletRequest request) throws AlipayApiException {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码再出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        return AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type());
    }

    /**
     * 保存订单数据
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 生成订单
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();
        //1、生成订单号
        String orderSn = IdWorker.getTimeId();
        //创建订单号
        OrderEntity entity = buildOrder(orderSn);

        //2、获取所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        //3、计算价格、积分等相关信息
        computePrice(entity, itemEntities);
        createTo.setOrder(entity);
        createTo.setOrderItems(itemEntities);

        return createTo;
    }

    /**
     * 计算价格
     * @param orderEntity
     * @param itemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal(0.0);//总价

        BigDecimal coupon = new BigDecimal(0.0);//打折
        BigDecimal integration = new BigDecimal(0.0);//积分
        BigDecimal promotion = new BigDecimal(0.0);//优惠券

        Integer gift = 0;//赠送积分
        Integer growth = 0;//成长值
        //订单的总额，叠加每一个订单项的总额信息
        for (OrderItemEntity entity : itemEntities) {
            coupon = coupon.add(entity.getCouponAmount());
            integration = integration.add(entity.getIntegrationAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            total = total.add(entity.getRealAmount());
            gift += entity.getGiftIntegration();
            growth += entity.getGiftGrowth();
        }
        //1、订单价格相关
        orderEntity.setTotalAmount(total);//订单总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));//应付总额
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        //设置积分等信息
        orderEntity.setIntegration(gift);
        orderEntity.setGrowth(growth);
    }

    /**
     * 构建订单
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(respVo.getId());
        entity.setCreateTime(new Date());

        OrderSubmitVo orderSubmitVo = submitThreadLocal.get();
        //获取收货地址信息
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });
        //设置运费信息
        entity.setFreightAmount(fareResp.getFare());//运费
        //设置收货人信息
        entity.setReceiverCity(fareResp.getAddress().getCity());//收货人城市
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());//收货人详细地址
        entity.setReceiverName(fareResp.getAddress().getName());//收货人姓名
        entity.setReceiverPhone(fareResp.getAddress().getPhone());//收货人电话
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());//收货人邮编
        entity.setReceiverProvince(fareResp.getAddress().getProvince());//收货人省份
        entity.setReceiverRegion(fareResp.getAddress().getRegion());//收货人区域

        //设置订单的相关状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());//订单状态：新建
        entity.setAutoConfirmDay(OrderConstant.AUTO_CONFIRM_DAY);//自动确认时间：7天
        entity.setDeleteStatus(0);//未删除

        return entity;
    }

    /**
     * 构建所有订单项数据
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //最后确定每个购物项的价格
        List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
        if(!CollectionUtils.isEmpty(cartItems)) {
            return cartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
        }

        return null;
    }

    /**
     * 构建某一个订单项
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //1、订单信息：订单号
        //2、商品的spu信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());//spuId
        itemEntity.setSpuBrand(data.getBrandId().toString());//品牌id
        itemEntity.setSpuName(data.getSpuName());//spu名称
        itemEntity.setCategoryId(data.getCatelogId());//分类id

        //3、商品的sku信息
        itemEntity.setSkuId(cartItem.getSkuId());//skuId
        itemEntity.setSkuName(cartItem.getTitle());//sku名称
        itemEntity.setSkuPic(cartItem.getImage());//sku图片
        itemEntity.setSkuPrice(cartItem.getPrice());//sku价格
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), "；");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());//sku数量

        //4、优惠信息[不做]
        //5、积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());//赠送的成长值
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());//赠送的积分

        //6、订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal(0));//促销优惠金额
        itemEntity.setCouponAmount(new BigDecimal(0));//优惠券优惠金额
        itemEntity.setIntegrationAmount(new BigDecimal(0));//积分抵扣金额
        //当前订单项的实际金额。   总额-各种优惠金额
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity()));
        BigDecimal subtract = orign.subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }
}