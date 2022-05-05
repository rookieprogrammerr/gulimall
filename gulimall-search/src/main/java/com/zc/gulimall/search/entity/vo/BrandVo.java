package com.zc.gulimall.search.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 品牌vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandVo {

    private Long brandId;

    private String brandName;

    private String brandImg;
}
