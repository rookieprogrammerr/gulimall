package com.zc.gulimall.product.entity.vo;

import com.zc.gulimall.product.entity.SkuImagesEntity;
import com.zc.gulimall.product.entity.SkuInfoEntity;
import com.zc.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SkuItemVo {
    //1、sku基本信息获取   pms_sku_info
    private SkuInfoEntity info;

    //2、sku的图片信息    pms_sku_images
    private List<SkuImagesEntity> images;

    //3、获取spu的销售属性组合
    private List<SkuItemSaleAttrsVo> saleAttr;

    //4、获取spu的介绍
    private SpuInfoDescEntity desp;

    //5、获取spu的规格参数信息
    private List<SpuItemBaseAttrGroupVo> groupAttrs;

}
