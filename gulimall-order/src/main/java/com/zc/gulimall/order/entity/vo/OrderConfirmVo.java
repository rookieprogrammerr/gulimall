package com.zc.gulimall.order.entity.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

//订单确认页需要用到的数据
public class OrderConfirmVo {
    //收货地址，ums_member_receive_address表
    @Setter
    @Getter
    private List<MemberAddressVo> address;
    //所有选中的购物项
    @Setter
    @Getter
    private List<OrderItemVo> items;
    //发票记录。。。。

    //优惠券信息
    @Setter
    @Getter
    private Integer integration;
    //库存信息
    @Setter
    @Getter
    private Map<Long, Boolean> stocks;
    //防重令牌
    @Setter
    @Getter
    private String orderToken;
    //订单总额
    private BigDecimal total;
    //应付总额
    private BigDecimal payPrice;

    public Integer getCount() {
        Integer count = 0;
        if(!CollectionUtils.isEmpty(items)){
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }

        return count;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(items)){
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount()));
                sum = sum.add(multiply);
            }
        }

        return sum;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
