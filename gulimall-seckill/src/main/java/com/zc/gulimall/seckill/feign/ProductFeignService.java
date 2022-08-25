package com.zc.gulimall.seckill.feign;

import com.zc.common.constant.GlobalServiceConstant;
import com.zc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(GlobalServiceConstant.PRODUCT_SERVICE)
public interface ProductFeignService {
    /**
     * 获取sku详细信息
     */
    @GetMapping("/product/skuinfo/info/{skuId}")
    public R getSkuInfo(@PathVariable("skuId") Long skuId);
}
