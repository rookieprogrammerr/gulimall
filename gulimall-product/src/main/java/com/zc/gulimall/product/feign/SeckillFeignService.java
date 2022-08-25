package com.zc.gulimall.product.feign;

import com.zc.common.constant.GlobalServiceConstant;
import com.zc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(GlobalServiceConstant.SECKILL_SERVICE)
public interface SeckillFeignService {
    /**
     * 获取指定商品的秒杀预告信息
     * @param skuId
     * @return
     */
    @GetMapping("/sku/seckill/{skuId}")
    R skuSeckillInfo(@PathVariable("skuId") Long skuId);
}
