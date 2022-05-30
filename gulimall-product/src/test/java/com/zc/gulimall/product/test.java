package com.zc.gulimall.product;

import com.zc.gulimall.product.dao.AttrGroupDao;
import com.zc.gulimall.product.dao.SkuSaleAttrValueDao;
import com.zc.gulimall.product.entity.vo.SkuItemSaleAttrsVo;
import com.zc.gulimall.product.entity.vo.SkuItemVo;
import com.zc.gulimall.product.entity.vo.SpuItemBaseAttrGroupVo;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

@SpringBootTest
public class test {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

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

    @Test
    void test2() {
        List<SpuItemBaseAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(4L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }

    @Test
    void test3() {
        List<SkuItemSaleAttrsVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(5L);
        System.out.println(saleAttrsBySpuId);
    }
}
