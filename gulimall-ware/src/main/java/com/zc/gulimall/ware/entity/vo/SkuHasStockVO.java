package com.zc.gulimall.ware.entity.vo;

import com.sun.xml.internal.fastinfoset.algorithm.BooleanEncodingAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuHasStockVO implements Serializable {

    private Long skuId;
    private Boolean hasStock;
}
