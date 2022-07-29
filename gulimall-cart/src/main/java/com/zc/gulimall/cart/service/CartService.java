package com.zc.gulimall.cart.service;

import com.zc.gulimall.cart.entity.vo.CartItem;

public interface CartService {
    /**
     * 添加购物车
     * @param skuId
     * @param num
     * @return
     */
    CartItem addToCart(Long skuId, Integer num);
}
