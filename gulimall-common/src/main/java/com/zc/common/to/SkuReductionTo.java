package com.zc.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author hl
 * @Data 2020/7/28
 */
@Data
public class SkuReductionTo {

    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;

}
