package com.zc.gulimall.product.entity.vo;

import com.zc.gulimall.product.entity.AttrEntity;
import com.zc.gulimall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @author hl
 * @Data 2020/7/27
 */
@Data
public class AttrGroupAndAttrVo extends AttrGroupEntity {

    /**
     * 属性列表
     */
    private List<AttrEntity> attrs;
}
