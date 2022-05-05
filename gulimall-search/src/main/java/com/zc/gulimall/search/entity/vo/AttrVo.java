package com.zc.gulimall.search.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 品牌属性vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttrVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValue;
}
