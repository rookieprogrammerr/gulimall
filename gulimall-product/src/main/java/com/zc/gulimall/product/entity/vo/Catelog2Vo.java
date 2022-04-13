package com.zc.gulimall.product.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 二级分类vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog2Vo implements Serializable {
    //一级父分类id
    private String catelog1Id;
    //三级子分类
    private List<Catelog3Vo> catelog3List;
    private String id;
    private String name;

}
