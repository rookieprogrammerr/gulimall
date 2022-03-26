package com.zc.gulimall.product.entity;

import com.zc.common.valid.ListValue;
import com.zc.common.valid.SaveValid;
import com.zc.common.valid.UpdateStatus;
import com.zc.common.valid.UpdateVaild;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 *
 * @author huanglin
 * @email 2465652971@qq.com
 * @date 2020-07-16 15:28:09
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(groups = {UpdateVaild.class}, message = "修改时品牌id不能为空")
    @Null(groups = {SaveValid.class}, message = "新增时品牌id必须为空")
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    @NotBlank(message = "品牌名不能为空", groups = {UpdateVaild.class, SaveValid.class})
    private String name;
    /**
     * 品牌logo地址
     */
    @NotBlank(message = "logo不能为空", groups = {SaveValid.class})
    @URL(message = "logo必须是一个合法的url", groups = {SaveValid.class, UpdateVaild.class})
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    @NotNull(message = "显示状态不能为空", groups = {SaveValid.class, UpdateVaild.class})
    @ListValue(values = {0, 1}, message = "显示状态只能是[0-不显示；1-显示]", groups = {UpdateStatus.class})
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotEmpty(message = "首字母不能为空", groups = {SaveValid.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "首字母必须为一个a-z或A-Z之间的字母", groups = {SaveValid.class, UpdateVaild.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(message = "排序字段不能为空", groups = {SaveValid.class})
    @Min(value = 0, message = "排序字段必须是大于0的整数", groups = {SaveValid.class, UpdateVaild.class})
    private Integer sort;

}
