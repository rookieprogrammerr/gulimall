package com.zc.gulimall.search.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatelogVo {
    //  分类id
    private Long catelogId;
    //  分类名字
    private String catelogName;
}
