package com.zc.gulimall.product.entity.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SkuItemSaleAttrsVo {
    private Long attrId;
    private String attrName;
    //  获得的值以","分割（红色，白色，黑色）
    private List<AttrValueWithSkuIdVo> attrValues;
}