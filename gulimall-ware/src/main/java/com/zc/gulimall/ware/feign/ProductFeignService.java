package com.zc.gulimall.ware.feign;

import com.zc.common.constant.GlobalServiceConstant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author hl
 * @Data 2020/7/29
 */
@FeignClient(GlobalServiceConstant.PRODUCT_SERVICE)
public interface ProductFeignService {

    @GetMapping("/product/skuinfo/getSkuName")
    public String getSkuName(@RequestParam Long skuId);
}
