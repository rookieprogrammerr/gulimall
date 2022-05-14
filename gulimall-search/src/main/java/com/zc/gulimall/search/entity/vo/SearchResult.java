package com.zc.gulimall.search.entity.vo;

import com.zc.common.to.es.SkuEsModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {

    //  查询到的所有商品信息
    private List<SkuEsModel> products;

    /**
     * 以下是分页信息
     */
    //  当前页码
    private Integer pageNum;
    //  总记录数
    private Long total;
    //  总页码
    private Integer totalPages;
    //  当前查询到的结果，所有涉及到的品牌
    private List<BrandVo> brands;
    //  当前查询到的结果，所有涉及到的所有分类
    private List<CatelogVo> catelogs;
    //  当前查询到的结果，所有涉及到的所有属性
    private List<AttrVo> attrs;


    //  ==============以上是返回给页面的所有信息===================

}
