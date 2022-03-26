package com.zc.common.to;

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
