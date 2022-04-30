package com.zc.gulimall.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class test {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    void test1() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        ops.set("name", "尚硅谷");

        String name = ops.get("name");
        System.out.println("之前保存在redis里的值是：" + name);
    }
}
