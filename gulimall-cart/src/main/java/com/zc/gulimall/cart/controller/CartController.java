package com.zc.gulimall.cart.controller;

import com.zc.gulimall.cart.entity.to.UserInfoTo;
import com.zc.gulimall.cart.entity.vo.CartItem;
import com.zc.gulimall.cart.interceptor.CartInterceptor;
import com.zc.gulimall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * 浏览器有一个cookie：user-key：标识用户身份，一个月后过期
     * 如果第一次使用购物车功能，都会给一个临时的用户身份；
     * 浏览器以后保存，每次访问都会带上这个cookie；
     *
     * 登录：session有
     * 没登陆：按照cookie里面带来的user-key来做
     * 第一次：如果没有临时用户，帮忙创建一个临时用户
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage() {
        //1、快速得到用户信息，id，user-key
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        System.out.println(userInfoTo);

        return "cartList";
    }

    /**
     * 添加商品到购物车
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            Model model) {

        CartItem cartItem = cartService.addToCart(skuId, num);

        model.addAttribute("item", cartItem);
        return "success";
    }
}
