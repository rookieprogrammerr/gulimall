package com.zc.gulimall.ware.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author hl
 * @Data 2020/7/29
 */
@Data
public class MergeReqVo {
    /**
     * 整单id
     */
    private Long purchaseId;
    /**
     * 合并项集合
     */
    private List<Long> items;
}
