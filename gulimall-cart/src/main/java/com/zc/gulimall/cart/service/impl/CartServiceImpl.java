package com.zc.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zc.common.constant.CartConstant;
import com.zc.common.to.SkuInfoEntity;
import com.zc.common.utils.R;
import com.zc.gulimall.cart.entity.to.UserInfoTo;
import com.zc.gulimall.cart.entity.vo.Cart;
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
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

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
     * 将商品添加到购物车
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
                cartItem.setCount(num);
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
     * 获取购物车中某个购物项
     * @param skuId
     * @return
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String str = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(str, CartItem.class);
        return cartItem;
    }

    /**
     * 获取整个购物车
     * @return
     */
    @Override
    public Cart getCart() {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() != null) {
            //登录了
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
            String tempCartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            //如果临时购物车的数据还没有进行合并【合并购物车】
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if(!CollectionUtils.isEmpty(tempCartItems)) {
                //临时购物车有东西，需要合并
                tempCartItems.forEach(item -> {
                    addToCart(item.getSkuId(), item.getCount());
                });
                //清除临时购物车的数据
                clearCart(tempCartKey);
            }

            //获取登录后的购物车【包含合并来的临时购物车数据和登录后的购物车数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        } else {
            //没登陆
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
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

    /**
     * 获取指定购物车的所有商品
     * @param cartKey
     * @return
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if(!CollectionUtils.isEmpty(values)) {
            List<CartItem> collect = values.stream().map(obj -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    /**
     * 清空购物车数据
     * @param cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    /**
     * 勾选购物项
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check==1);
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    /**
     * 修改购物项数量
     * @param skuId
     * @param num
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        if(num == 0) {
            deleteItem(skuId);
        } else {
            BoundHashOperations<String, Object, Object> cartOps = getCartOps();
            CartItem cartItem = getCartItem(skuId);
            cartItem.setCount(num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
        }
    }

    /**
     * 删除购物项
     * @param skuId
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }
}
