package com.zc.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zc.common.constant.CartConstant;
import com.zc.common.to.SkuInfoEntity;
import com.zc.common.utils.R;
import com.zc.gulimall.cart.entity.to.UserInfoTo;
import com.zc.gulimall.cart.entity.vo.CartItem;
import com.zc.gulimall.cart.feign.ProductFeignService;
import com.zc.gulimall.cart.interceptor.CartInterceptor;
import com.zc.gulimall.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private ThreadPoolExecutor executor;

    /**
     * 添加购物车
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String res = (String) cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(res)) {
            //购物车无此商品。添加商品到购物车
            CartItem cartItem = new CartItem();
            //1、远程查询当前要添加的商品的信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R r = productFeignService.getSkuInfo(skuId);
                SkuInfoEntity data = r.getData("skuInfo", new TypeReference<SkuInfoEntity>() {
                });

                cartItem.setCheck(true);
                cartItem.setCount(1);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(data.getPrice());
            }, executor);

            //3、远程查询sku的组合信息
            CompletableFuture<Void> getSkuSaleAttrValuesTask = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValuesTask).join();

            String str = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), str);

            return cartItem;
        } else {
            //购物车有此商品。更新商品数量
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));

            return cartItem;
        }
    }

    /**
     * 获取到我们要操作的购物车
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        StringBuilder cartKey = new StringBuilder(CartConstant.CART_PREFIX);
        if(userInfoTo.getUserId() != null) {
            //登录了
            cartKey.append(userInfoTo.getUserId());
        } else {
            //没登陆
            cartKey.append(userInfoTo.getUserKey());
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey.toString());

        return operations;
    }
}
