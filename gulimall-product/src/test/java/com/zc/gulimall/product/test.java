package com.zc.gulimall.product;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class test {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @Test
    void test1() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        ops.set("name", "尚硅谷");

        String name = ops.get("name");
        System.out.println("之前保存在redis里的值是：" + name);
    }

    @Test
    void redisson() {
        System.out.println(redissonClient);
    }
}
