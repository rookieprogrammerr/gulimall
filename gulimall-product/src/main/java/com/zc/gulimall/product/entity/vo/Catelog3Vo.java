package com.zc.gulimall.product.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 三级分类vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog3Vo implements Serializable {
    //  父分类，2级分类id
    private String catelog2Id;
    private String id;
    private String name;
}
