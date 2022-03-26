package com.zc.common.to.es;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuEsModel implements Serializable {
    /**  skuid */
    private Long skuId;
    //  spuid
    private Long spuId;
    //  sku标题
    private String skuTitle;
    //  sku价格
    private BigDecimal skuPrice;
    //  sku图片
    private String skuImg;
    //  销量
    private Long saleCount;
    //  是否库存
    private Boolean hasStock;
    //  热度评分
    private Long hotScore;
    //  品牌id
    private Long brandId;
    //  分类id
    private Long catelogId;
    //  品牌名字
    private String brandName;
    //  品牌图片
    private String brandImg;
    //  分类名字
    private String catelogName;
    //  规格属性信息
    private List<Attrs> attrs;
}
