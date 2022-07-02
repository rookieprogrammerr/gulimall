package com.zc.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zc.common.constant.GlobalUrlConstant;
import com.zc.common.utils.HttpUtils;
import com.zc.common.utils.R;
import com.zc.gulimall.auth.entity.vo.MemberRespVo;
import com.zc.gulimall.auth.entity.vo.SocialUser;
import com.zc.gulimall.auth.feign.MemberFeignService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求
 */
@Controller
@Slf4j
public class OAuth2Controller {
    @Value("${weibo.app_key}")
    private String appKey;
    @Value("${weibo.app_secret}")
    private String appSecurity;
    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 社交登录成功回调
     * @param code
     * @return
     * @throws Exception
     */
    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {
        //避免引用不明确
        Map<String, String> header = new HashMap<>();
        String body = null;

        Map<String, String> querys = new HashMap<>();
        querys.put("client_id", appKey);
        querys.put("client_secret", appSecurity);
        querys.put("grant_type", "authorization_code");
        querys.put("redirect_uri", GlobalUrlConstant.AUTH_SERVER_URL + "/oauth2.0/weibo/success");
        querys.put("code", code);

        //1、根据code换取accessToken；
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", header, querys, body);

        //2、处理
        if(response.getStatusLine().getStatusCode() == 200) {
            //获取到了accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            //知道当前是哪个社交用户
            //1）、当前用户如果是第一次进入网站，自动注册进来(为当前社交用户生成一个会员信息账号，以后这个社交帐号就对应指定的会员)
            //登录或者注册这个用户
            R r = memberFeignService.oauthlogin(socialUser);
            if(r.getCode() == 0) {
                //成功
                MemberRespVo data = r.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("登陆成功，用户信息:{}", data.toString());
                return "redirect:" + GlobalUrlConstant.INDEX_URL;
            } else {
                return "redirect:" + GlobalUrlConstant.AUTH_SERVER_URL + "/login.html";
            }
        } else {
            return "redirect:" + GlobalUrlConstant.AUTH_SERVER_URL + "/login.html";
        }
    }
}
