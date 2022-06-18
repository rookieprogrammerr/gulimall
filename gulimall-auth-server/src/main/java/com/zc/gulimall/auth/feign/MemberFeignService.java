package com.zc.gulimall.auth.feign;

import com.zc.common.utils.R;
import com.zc.gulimall.auth.entity.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    /**
     * 注册会员
     */
    @PostMapping("/member/member/register")
    R regist(@RequestBody UserRegistVo vo);
}
