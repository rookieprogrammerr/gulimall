package com.zc.gulimall.seckill.scheduled;

import com.zc.gulimall.seckill.constant.SeckillConstant;
import com.zc.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品的定时上架
 */
@Slf4j
@Service
public class SeckillSkuScheduled {
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private RedissonClient redissonClient;



    /**
     * 上架最新3天的商品
     */
    //TODO 幂等性问题
    @Scheduled(cron = "*/30 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        //1、重复上架无需处理
        log.info("上架秒杀的商品信息...");
        //分布式锁。锁的业务执行完成，状态已经更新完成，释放锁以后其他人获取到就会拿到最新的状态
        RLock lock = redissonClient.getLock(SeckillConstant.UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }
}
