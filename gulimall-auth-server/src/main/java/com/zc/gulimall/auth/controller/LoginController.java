package com.zc.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.zc.common.constant.AuthServerConstant;
import com.zc.common.constant.GlobalUrlConstant;
import com.zc.common.exception.BizCodeEnum;
import com.zc.common.utils.R;
import com.zc.gulimall.auth.entity.vo.UserRegistVo;
import com.zc.gulimall.auth.feign.MemberFeignService;
import com.zc.gulimall.auth.feign.ThirdPartFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 发送一个请求直接跳转到一个页面。
     * SpringMVC viewController：将请求和页面映射过来
     *
     */

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {

        //TODO 1、接口防刷

        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(StringUtils.isNotEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis() - l < 60000) {
                //60秒内不能再发
                return R.error(BizCodeEnum.VALID_SMS_CODE.getCode(), BizCodeEnum.VALID_SMS_CODE.getMsg());
            }
        }

        //2、验证码的再次校验。redis。存的时候：key：phone，value：验证码
        String code = UUID.randomUUID().toString().substring(0, 5);
        String substring = code + "_" + System.currentTimeMillis();

        //redis缓存验证码，防止同一个phone在60秒内再次发送验证码
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, substring, 10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, code);
        return R.ok();
    }

    /**
     *
     *  //TODO 重定向携带数据，利用session原理。将数据放在session中。只要跳到下一个页面取出数据后，session里面的数据就会删掉。
     *
     *  //TODO 1、分布式下的session问题；
     * @param userRegistVo
     * @param result
     * @param redirectAttributes:模拟重定向携带数据
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo userRegistVo, BindingResult result, RedirectAttributes redirectAttributes) {
        if(result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            //校验出错，转发到注册页
            return "redirect:" + GlobalUrlConstant.AUTH_SERVER_URL + "/reg.html";
        }

        //真正注册。调用远程服务进行注册
        //1、校验验证码
        String code = userRegistVo.getCode();

        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
        if(StringUtils.isNotEmpty(redisCode)) {
            if(code.equals(redisCode.split("_")[0])) {
                //验证码通过，删除验证码.令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
                //2、调用远程服务进行注册
                R r = memberFeignService.regist(userRegistVo);
                if(r.getCode() == 0) {
                    //成功
                    return "redirect:" + GlobalUrlConstant.AUTH_SERVER_URL + "/login.html";
                } else {
                    //失败
                    HashMap<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData(new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors", errors);

                    return "redirect:" + GlobalUrlConstant.AUTH_SERVER_URL + "/reg.html";
                }
                //删除验证码
            } else {
                //验证码出错
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                //校验出错，转发到注册页
                return "redirect:" + GlobalUrlConstant.AUTH_SERVER_URL + "/reg.html";
            }
        } else {
            //验证码出错
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            //校验出错，转发到注册页
            return "redirect:" + GlobalUrlConstant.AUTH_SERVER_URL + "/reg.html";
        }

        //注册成功回到首页，回到登录页
        //return "redirect:/login.html";
    }
}
