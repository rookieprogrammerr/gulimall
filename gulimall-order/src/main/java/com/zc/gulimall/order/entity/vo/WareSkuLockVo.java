package com.zc.gulimall.order.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    private String orderSn;//订单号
    private List<OrderItemVo> locks;//需要锁住的所有库存信息
}
