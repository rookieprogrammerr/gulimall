package com.zc.gulimall.cart.controller;

import com.zc.common.constant.GlobalUrlConstant;
import com.zc.gulimall.cart.entity.to.UserInfoTo;
import com.zc.gulimall.cart.entity.vo.Cart;
import com.zc.gulimall.cart.entity.vo.CartItem;
import com.zc.gulimall.cart.interceptor.CartInterceptor;
import com.zc.gulimall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * 获取用户购物车内选中的购物项
     * @return
     */
    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems() {
        return cartService.getUserCartItems();
    }
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
    public String cartListPage(Model model) {
        //1、快速得到用户信息，id，user-key
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     *      addFlashAttribute();将数据放在session中，可以在页面取出，但是只能取一次
     *      addAttribute();将数据放在url后
     * @param skuId
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) {

        cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:" + GlobalUrlConstant.CART_SERVER_URL + "/addToCartSuccessPage.html";
    }

    /**
     * 跳转到成功页
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccessPage.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        //重定向到成功界面，再次查询购物车数据即可
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }

    /**
     * 勾选购物项
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:" + GlobalUrlConstant.CART_SERVER_URL + "/cart.html";
    }

    /**
     * 修改购物项数量
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return "redirect:" + GlobalUrlConstant.CART_SERVER_URL + "/cart.html";
    }

    /**
     * 删除购物项
     * @param skuId
     * @return
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:" + GlobalUrlConstant.CART_SERVER_URL + "/cart.html";
    }
}
