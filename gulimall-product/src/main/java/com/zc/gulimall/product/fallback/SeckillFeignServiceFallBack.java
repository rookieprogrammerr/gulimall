package com.zc.gulimall.product.fallback;

import com.zc.common.exception.BizCodeEnum;
import com.zc.common.utils.R;
import com.zc.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R skuSeckillInfo(Long skuId) {
        log.info("熔断方法调用。。。skuSeckillInfo");
        return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMsg());
    }
}
