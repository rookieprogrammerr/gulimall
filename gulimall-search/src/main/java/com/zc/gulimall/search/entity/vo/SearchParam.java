package com.zc.gulimall.search.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 封装页面所有可能传递过来的查询条件
 *
 * catelog3Id=225&keyword=小米&sort=saleCount_asc&hasStock=0/1&brandId=1&brandId=2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchParam {

    //  页面传递过来的全文匹配关键字
    private String keyword;
    //  三级分类id
    private Long catelog3Id;

    /**
     * 排序条件
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;

    /**
     * 好多的过滤条件
     * hasStock(是否有货)、skuPrice区间、brandId、catelog3Id、attrs
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     * brandId=1
     * attrs=2_5寸:6寸
     */
    //  是否只显示有货
    private Integer hasStock;
    //  价格区间
    private String skuPrice;
    //  按照品牌id进行查询，可多选
    private List<Long> brandId;
    //  按照属性进行筛选
    private List<String> attrs;
    //  页码
    private Integer pageNum = 1;

}
