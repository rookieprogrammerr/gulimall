/**
 * Copyright 2020 bejson.com
 */
package com.zc.gulimall.product.entity.vo;

import com.zc.common.valid.SaveValid;
import com.zc.common.valid.UpdateVaild;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2020-07-27 22:16:54
 * <p>
 * <p>
 * TODO 后面来写校验注解
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuSaveVo {

    @NotNull(groups = {SaveValid.class, UpdateVaild.class}, message = "名称不能为空")
    private String spuName;
    private String spuDescription;
    private Long catelogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;
    private List<String> decript;
    private List<String> images;
    private Bounds bounds;
    private List<BaseAttrs> baseAttrs;
    private List<Skus> skus;
}