package com.zc.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hl
 * @Data 2020/7/28
 */
@Data
public class SpuBoundsTo {

    private Long spuId;
    /**
     * 购买积分
     */
    private BigDecimal buyBounds;
    /**
     * 成长积分
     */
    private BigDecimal growBounds;
}
