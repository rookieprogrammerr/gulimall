package com.zc.gulimall.seckill.service;

import com.zc.gulimall.seckill.entity.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillService {
    /**
     * 上架最新3天的商品
     */
    void uploadSeckillSkuLatest3Days();

    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    /**
     * 获取指定商品的秒杀预告信息
     * @param skuId
     * @return
     */
    SeckillSkuRedisTo skuSeckillInfo(Long skuId);

    /**
     * 限时秒杀
     * @param killId
     * @param key
     * @param num
     * @return
     */
    String kill(String killId, String key, Integer num);
}
