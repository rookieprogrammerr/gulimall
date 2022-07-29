package com.zc.gulimall.cart.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车内的购物项
 */
public class CartItem {
    //商品id
    private Long skuId;
    //是否选中
    private Boolean check = true;
    //商品标题
    private String title;
    //商品图片
    private String image;
    //商品套餐信息
    private List<String> skuAttr;
    //价格
    private BigDecimal price;
    //数量
    private Integer count;
    //小计
    private BigDecimal totalPrice;

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * 计算当前项总价
     * @return
     */
    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(this.count));
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
