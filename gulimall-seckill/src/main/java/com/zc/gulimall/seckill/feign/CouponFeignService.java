package com.zc.gulimall.seckill.feign;

import com.zc.common.constant.GlobalServiceConstant;
import com.zc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(GlobalServiceConstant.COUPON_SERVICE)
public interface CouponFeignService {

    /**
     * 查询最近三天需要参与秒杀的活动
     * @return
     */
    @GetMapping("/coupon/seckillsession/getLates3DaySession")
    R getLates3DaySession();
}
