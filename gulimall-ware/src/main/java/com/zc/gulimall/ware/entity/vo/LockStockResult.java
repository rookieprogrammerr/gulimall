package com.zc.gulimall.ware.entity.vo;

import lombok.Data;

@Data
public class LockStockResult {
    private Long skuId;//锁定的skuId
    private Integer num;//锁定的数量
    private boolean locked;//是否锁定成功
}
