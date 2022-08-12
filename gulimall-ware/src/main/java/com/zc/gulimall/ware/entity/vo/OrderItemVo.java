package com.zc.gulimall.ware.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    //商品id
    private Long skuId;
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
    //TODO 查询是否有货
    private boolean hasStock;
    //重量
    private BigDecimal weight;
}
