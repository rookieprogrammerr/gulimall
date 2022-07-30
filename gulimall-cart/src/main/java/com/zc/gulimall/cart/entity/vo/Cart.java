package com.zc.gulimall.cart.entity.vo;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整个购物车
 * 需要计算的属性，必须重写他的get方法，保证每次获取属性都会进行计算
 */
public class Cart {
    private List<CartItem> items;
    //商品数量
    private Integer countNum;
    //商品类型属性
    private Integer countType;
    //总价
    private BigDecimal totalAmount;
    //减免价格
    private BigDecimal reduce = new BigDecimal(0);

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if(!CollectionUtils.isEmpty(items)){
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if(!CollectionUtils.isEmpty(items)){
            count = items.size();
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal(0);
        //1、计算购物项总价
        if(!CollectionUtils.isEmpty(items)){
            for (CartItem item : items) {
                if(item.getCheck()) {
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }
        //2、减去优惠总价
        amount = amount.subtract(getReduce());

        return amount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
