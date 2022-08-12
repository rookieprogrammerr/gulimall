package com.zc.gulimall.ware.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuWareHasStock {
    private Long skuId;
    private List<Long> wareIds;
    private Integer num;
}
