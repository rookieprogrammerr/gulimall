package com.zc.gulimall.auth.controller;

import com.zc.common.constant.GlobalUrlConstant;
import com.zc.common.utils.HttpUtils;
import org.apache.http.HttpResponse;
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
public class OAuth2Controller {
    @Value("${weibo.app_key}")
    private String appKey;
    @Value("${weibo.app_secret}")
    private String appSecurity;

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
        HttpResponse response = HttpUtils.doPost("api.weibo.com", "/oauth2/access_token", "post", header, querys, body);
        //2、登录成功就跳回首页
        return "redirect:" + GlobalUrlConstant.INDEX_URL;
    }
}
