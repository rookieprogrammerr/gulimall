package com.zc.gulimall.product.feign;

import com.zc.common.constant.GlobalServiceConstant;
import com.zc.common.to.SkuReductionTo;
import com.zc.common.to.SpuBoundsTo;
import com.zc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author hl
 * @Data 2020/7/28
 */
@FeignClient(GlobalServiceConstant.COUPON_SERVICE)
public interface CouponFeignService {

    @PostMapping("/coupon/spubounds/saveSpuBoundTo")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
