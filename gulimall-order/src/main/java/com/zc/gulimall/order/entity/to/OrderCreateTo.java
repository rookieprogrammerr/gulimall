package com.zc.gulimall.order.entity.to;

import com.zc.gulimall.order.entity.OrderEntity;
import com.zc.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    private OrderEntity order;//创建好的订单信息
    private List<OrderItemEntity> orderItems;//订单项信息
    private BigDecimal payPrice;//订单计算的应付价格
    private BigDecimal fare;//运费
}
