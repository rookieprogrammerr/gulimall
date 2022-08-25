package com.zc.gulimall.seckill.entity.to;

import com.baomidou.mybatisplus.annotation.TableId;
import com.zc.gulimall.seckill.entity.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuRedisTo {
    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品秒杀随机码
     */
    private String randomCode;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;
    /**
     * sku详细信息
     */
    private SkuInfoVo skuInfo;
    /**
     * 当前商品秒杀的开始时间
     */
    private long startTime;
    /**
     * 当前商品秒杀的结束时间
     */
    private long endTime;
}
