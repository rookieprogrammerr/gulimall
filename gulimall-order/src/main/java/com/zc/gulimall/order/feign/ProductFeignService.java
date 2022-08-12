package com.zc.gulimall.order.feign;

import com.zc.common.constant.GlobalServiceConstant;
import com.zc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(GlobalServiceConstant.PRODUCT_SERVICE)
public interface ProductFeignService {
    /**
     * 根据skuId获取spu的信息
     * @param skuId
     * @return
     */
    @GetMapping("/product/spuinfo/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id") Long skuId);
}
