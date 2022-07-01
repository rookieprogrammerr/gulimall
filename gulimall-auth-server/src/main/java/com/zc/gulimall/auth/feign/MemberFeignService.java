package com.zc.gulimall.auth.feign;

import com.zc.common.utils.R;
import com.zc.gulimall.auth.entity.vo.SocialUser;
import com.zc.gulimall.auth.entity.vo.UserLoginVo;
import com.zc.gulimall.auth.entity.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    /**
     * 注册会员
     * @param vo
     * @return
     */
    @PostMapping("/member/member/register")
    R regist(@RequestBody UserRegistVo vo);

    /**
     * 会员登录
     * @param vo
     * @return
     */
    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    /**
     * 社交登录
     * @param socialUser
     * @return
     */
    @PostMapping("/member/member/oauth/login")
    public R oauthlogin(@RequestBody SocialUser socialUser) throws Exception;
}
