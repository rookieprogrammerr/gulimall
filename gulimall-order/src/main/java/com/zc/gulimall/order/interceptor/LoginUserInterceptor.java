package com.zc.gulimall.order.interceptor;

import com.zc.common.constant.AuthServerConstant;
import com.zc.common.constant.GlobalUrlConstant;
import com.zc.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute != null) {
            loginUser.set(attribute);
            return true;
        } else {
            //没登陆就去登录
            request.getSession().setAttribute("msg", "请先进行登录");
            response.sendRedirect(GlobalUrlConstant.AUTH_SERVER_URL + "/login.html");
            return false;
        }
    }
}
