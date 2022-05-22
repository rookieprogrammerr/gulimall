package com.zc.gulimall.search.entity.vo;

import com.zc.common.valid.ListValue;
import com.zc.common.valid.SaveValid;
import com.zc.common.valid.UpdateStatus;
import com.zc.common.valid.UpdateVaild;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

@Data
public class BrandEntity {

    /**
     * 品牌id
     */
    private Long brandId;
    /**
     * 品牌名
     */
    private String name;
    /**
     * 品牌logo地址
     */
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    private Integer showStatus;
    /**
     * 检索首字母
     */
    private String firstLetter;
    /**
     * 排序
     */
    private Integer sort;
}
