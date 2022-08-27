package com.zc.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zc.common.to.mq.SeckillOrderTo;
import com.zc.common.utils.R;
import com.zc.common.vo.MemberRespVo;
import com.zc.gulimall.seckill.constant.SeckillConstant;
import com.zc.gulimall.seckill.entity.to.SeckillSkuRedisTo;
import com.zc.gulimall.seckill.entity.vo.SeckillSessionsWithSkus;
import com.zc.gulimall.seckill.entity.vo.SkuInfoVo;
import com.zc.gulimall.seckill.feign.CouponFeignService;
import com.zc.gulimall.seckill.feign.ProductFeignService;
import com.zc.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.zc.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 上架最新3天的商品
     */
    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1、扫描最近三天需要参与秒杀的活动
        R r = couponFeignService.getLates3DaySession();
        if(r.getCode() == 0) {
            //上架商品
            List<SeckillSessionsWithSkus> sessionData = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //缓存到redis中
            //1、缓存活动信息
            saveSessionInfos(sessionData);
            //2、缓存活动的关联商品信息
            saveSessionSkuInfos(sessionData);
        }

    }


    /**
     * 缓存活动信息
     * @param sessions
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        if(!CollectionUtils.isEmpty(sessions)) {
            sessions.stream().forEach(session -> {
                long startTime = session.getStartTime().getTime();
                long endTime = session.getEndTime().getTime();
                //活动key
                String key = SeckillConstant.SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;

                //缓存活动信息
                if(Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
                    List<String> collect = session.getRelationSkus().stream().map(vo -> { return vo.getPromotionSessionId() + "_" + vo.getSkuId().toString();}).collect(Collectors.toList());
                    redisTemplate.opsForList().leftPushAll(key, collect);
                }

            });
        }
    }

    /**
     * 缓存活动关联商品信息
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions) {
        if(!CollectionUtils.isEmpty(sessions)) {
            sessions.stream().forEach(session -> {
                //准备hash操作
                BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SeckillConstant.SKUKILL_CACHE_PREFIX);
                session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                    String token = UUID.randomUUID().toString().replace("-", "");

                    if(Boolean.FALSE.equals(ops.hasKey(seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString()))) {
                        //缓存商品
                        SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                        //1、sku的基本数据
                        R r = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                        if(r.getCode() == 0) {
                            SkuInfoVo info = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                            });
                            redisTo.setSkuInfo(info);
                        }

                        //2、sku的秒杀信息
                        BeanUtils.copyProperties(seckillSkuVo, redisTo);

                        //3、设置上当前商品的秒杀时间信息
                        redisTo.setStartTime(session.getStartTime().getTime());
                        redisTo.setEndTime(session.getEndTime().getTime());

                        //4、随机码:避免恶意秒杀
                        redisTo.setRandomCode(token);

                        //如果当前这个场次的商品的库存信息已经上架就不需要上架
                        //5、使用库存作为分布式的信号量  限流
                        RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + token);
                        //商品可以秒杀的数量作为信号量
                        semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());

                        String jsonString = JSON.toJSONString(redisTo);
                        ops.put(seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId(), jsonString);
                    }
                });
            });
        }
    }

    /**
     * getCurrentSeckillSkus()的限流方法
     * @return
     */
    public List<SeckillSkuRedisTo> blockHandler(BlockException e) {
        log.error("getCurrentSeckillSkus被限流了..,{}", e.getMessage());
        return null;
    }
    /**
     * 返回当前时间可以参与的秒杀商品信息
     * blockHandler 函数会在原方法被限流/降级/系统保护的时候调用，而 fallback 函数会针对所有类型的异常。
     * @return
     */
    @SentinelResource(value = "getCurrentSeckillSkusResource", blockHandler = "blockHandler")
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //1、确定当前时间属于哪个秒杀场次
        long time = new Date().getTime();

        try(Entry entry = SphU.entry("seckillSkus")) {
            Set<String> keys = redisTemplate.keys(SeckillConstant.SESSIONS_CACHE_PREFIX + "*");
            if(!CollectionUtils.isEmpty(keys)) {
                for (String key : keys) {
                    String[] s = key.replace(SeckillConstant.SESSIONS_CACHE_PREFIX, "").split("_");
                    long start = Long.parseLong(s[0]);
                    long end = Long.parseLong(s[1]);
                    if(time >= start && time <= end) {
                        //当前时间符合当前场次
                        //2、获取这个秒杀场次需要的所有商品信息
                        List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SeckillConstant.SKUKILL_CACHE_PREFIX);
                        List<String> list = hashOps.multiGet(range);
                        if(!CollectionUtils.isEmpty(list)) {
                            List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                                SeckillSkuRedisTo redisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
//                            redisTo.setRandomCode(null);  //当前秒杀开始就需要随机码
                                return redisTo;
                            }).collect(Collectors.toList());

                            return collect;
                        }
                        break;
                    }
                }
            }
        } catch (BlockException e) {
            log.error("资源被限流，{}", e.getMessage());
        }

        return null;
    }

    /**
     * 获取指定商品的秒杀预告信息
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo skuSeckillInfo(Long skuId) {
        //1、找到所有需要参与秒杀的商品的key信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SeckillConstant.SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if(!CollectionUtils.isEmpty(keys)) {
            //正则表达式匹配key
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo skuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    //随机码处理
                    long current = new Date().getTime();
                    if(current >= skuRedisTo.getStartTime() && current <= skuRedisTo.getEndTime()) {

                    } else {

                    }
                    return skuRedisTo;
                }
            }
        }
        return null;
    }

    /**
     * 限时秒杀
     * @param killId
     * @param key
     * @param num
     * @return
     */
    //TODO 1、上架秒杀商品的时候，每一个数据都有过期时间。
    //TODO 2、秒杀后续的流程，简化了收货地址等信息。
    @Override
    public String kill(String killId, String key, Integer num) {
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();
        //1、获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SeckillConstant.SKUKILL_CACHE_PREFIX);

        String json = hashOps.get(killId);
        if(StringUtils.isEmpty(json)) {
            return null;
        } else {
            SeckillSkuRedisTo skuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
            long time = new Date().getTime();
            //1、校验时间合法性
            if(time >= skuRedisTo.getStartTime() && time <= skuRedisTo.getEndTime()) {
                //2、校验随机码和商品id
                String skuId = skuRedisTo.getPromotionSessionId() + "_" + skuRedisTo.getSkuId();
                String randomCode = skuRedisTo.getRandomCode();
                if(randomCode.equals(key) && killId.equals(skuId)) {
                    //3、验证购物数量是否合法
                    if (num <= skuRedisTo.getSeckillLimit().intValue()) {
                        //4、验证这个人是否已经购买过。幂等性；如果只要秒杀成功，就去占位。  userId_session_skuId_
                        String redisKey = respVo.getId() + "_" + skuId;
                        //自动过期
                        long ttl = skuRedisTo.getEndTime() - time;
                        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if(Boolean.TRUE.equals(ifAbsent)) {
                            //占位成功代表从来没买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + randomCode);
                                boolean b = semaphore.tryAcquire(num);
                                if(b) {
                                    //秒杀成功
                                    //快速下单。发送MQ消息
                                    String timeId = IdWorker.getTimeId();
                                    SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                    seckillOrderTo.setOrderSn(timeId);
                                    seckillOrderTo.setMemberId(respVo.getId());
                                    seckillOrderTo.setNum(num);
                                    seckillOrderTo.setPromotionSessionId(skuRedisTo.getPromotionSessionId());
                                    seckillOrderTo.setSkuId(skuRedisTo.getSkuId());
                                    seckillOrderTo.setSeckillPrice(skuRedisTo.getSeckillPrice());
                                    rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTo);
                                    return timeId;
                                }
                                return null;
                        } else {
                            //说明已经买过了
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;
    }
}
