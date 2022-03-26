package com.zc.gulimall.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author hl
 * @Data 2020/7/29
 */
@FeignClient(value = "gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/skuinfo/getSkuName")
    public String getSkuName(@RequestParam Long skuId);
}
