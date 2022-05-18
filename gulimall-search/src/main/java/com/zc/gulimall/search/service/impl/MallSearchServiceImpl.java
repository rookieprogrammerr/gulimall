package com.zc.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.zc.common.to.es.SkuEsModel;
import com.zc.gulimall.search.config.GulimallElasticSearchConfig;
import com.zc.gulimall.search.constant.EsConstant;
import com.zc.gulimall.search.entity.vo.*;
import com.zc.gulimall.search.service.MallSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam searchParam) {
        //  动态构建出查询需要的DSL语句
        SearchResult result = null;

        //  1、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);

        try {
            //  2、执行检索请求
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //  3、分析相应数据，封装成我们想要的格式
            result = buildSearchResult(response, searchParam);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }


    /**
     * 准备检索请求
     * #模糊匹配，过滤（按照属性、分类、品牌、价格区间、库存），排序，分页，高亮，聚合分析
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        //  构建DSL语句的
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /**
         * 查询条件：模糊匹配，过滤（按照属性、分类、品牌、价格区间、库存）
         */

        //  1、构建bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //  1.1、must
        if(StringUtils.isNotEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        //  1.2、bool - filter   按照三级分类id查询
        if(param.getCatelog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catelogId", param.getCatelog3Id()));
        }

        //  1.2、bool - filter   按照品牌id查询
        if(!CollectionUtils.isEmpty(param.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        //  1.2、bool - filter   按照所有指定的属性进行查询
        if(!CollectionUtils.isEmpty(param.getAttrs())) {
            for (String attr : param.getAttrs()) {
                //attrs=1_5寸:8寸&attrs=2_16G:8G
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                //attrs=1_5寸:8寸
                String[] attrSplit = attr.split("_");
                String attrId = attrSplit[0];   //检索的属性id
                String[] attrValues = attrSplit[1].split(":");  //这个属性的检索用的值
                BoolQueryBuilder must = nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrId", attrId));
                must.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //  每一个必须都得生成一个嵌入式nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }

        //  1.2、bool - filter   按照是否拥有库存进行查询
        if(param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termsQuery("hasStock", param.getHasStock() == 1));
        }

        //  1.2、bool - filter   按照价格区间进行查询
        if(StringUtils.isNotEmpty(param.getSkuPrice())) {
            //1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");

            String[] priceSplit = param.getSkuPrice().split("_");
            if(priceSplit.length == 2) {
                //  区间
                rangeQuery.gte(priceSplit[0]).lte(priceSplit[1]);
            } else if(priceSplit.length == 1) {
                if(param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(priceSplit[0]);
                }

                if(param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(priceSplit[0]);
                }
            }

            boolQuery.filter(rangeQuery);
        }


        sourceBuilder.query(boolQuery);

        /**
         * 排序，分页，高亮
         */

        //  2.1、排序
        if(StringUtils.isNotEmpty(param.getSort())) {
            String[] sortSplit = param.getSort().split("_");
            SortOrder sortOrder = sortSplit[1].equals("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(sortSplit[0], sortOrder);
        }

        //  2.2、分页  pageSize:5
        //  pageNum:1  from:0 size:5
        //  pageNum:2  from:1 size:5
        //  from = (pageNum - 1) * size
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //  2.3、高亮
        if(StringUtils.isNotEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();

            builder.field("skuTitle").preTags("<b style='color:red'>").postTags("</b>");

            sourceBuilder.highlighter(builder);
        }

        /**
         * 聚合分析
         */
        //  1、品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        //品牌聚合的子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        //TODO 1、聚合brand
        sourceBuilder.aggregation(brandAgg);

        //  2、分类聚合
        TermsAggregationBuilder catelogAgg = AggregationBuilders.terms("catelog_agg").field("catelogId").size(20);
        catelogAgg.subAggregation(AggregationBuilders.terms("catelog_name_agg").field("catelogName").size(1));
        //TODO 2、聚合catelog
        sourceBuilder.aggregation(catelogAgg);

        //  3、属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        //聚合出当前所有的attrId
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //聚合分析出当前attr_id对应的名字
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //聚合分析出当前attr_id对应的所有可能的属性值attrValue
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrAgg.subAggregation(attrIdAgg);
        //TODO 3、聚合attr
        sourceBuilder.aggregation(attrAgg);

        String s = sourceBuilder.toString();
        System.out.println("构建的DSL" + s);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }


    /**
     * 构建结果数据
     * @param response
     * @param param
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        //  1、返回的所有查询到的商品
        SearchHits hits = response.getHits();

        //  所有商品信息
        List<SkuEsModel> esModels = new ArrayList<>();
        if(ArrayUtils.isNotEmpty(hits.getHits())) {
            for (SearchHit hit : hits.getHits()) {
                //  获取的hits是以json格式返回的
                String sourceAsString = hit.getSourceAsString();
                //  转换为指定的对象类型
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);

                if (StringUtils.isNotEmpty(param.getKeyword())) {
                    //  设置高亮字段
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.fragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }

                esModels.add(skuEsModel);
            }
        }
        result.setProducts(esModels);

        //  2、当前所有商品涉及到的所有属性信息
        ArrayList<AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        //  所有的属性bucket信息
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            AttrVo attrVo = new AttrVo();
            //  属性id
            Long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            //  属性名
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            //  属性值
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            //收集所有属性值的值
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }

        result.setAttrs(attrVos);

        //  3、当前所有商品设计到的所有品牌信息
        ArrayList<BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        //  所有的品牌bucket信息
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            BrandVo brandVo = new BrandVo();
            //  得到品牌id
            Long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            //  得到品牌的名字
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            //  得到品牌的图片
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //  4、当前所有商品涉及到的所有分类信息
        ParsedLongTerms catelogAgg = response.getAggregations().get("catelog_agg");
        //  所有的分类bucket信息
        ArrayList<CatelogVo> catelogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catelogAgg.getBuckets()) {
            CatelogVo catelogVo = new CatelogVo();
            //  得到分类id
            Long catelogId = bucket.getKeyAsNumber().longValue();
            catelogVo.setCatelogId(catelogId);

            //  得到分类名
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catelog_name_agg");
            String catelogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catelogVo.setCatelogName(catelogName);

            catelogVos.add(catelogVo);
        }

        result.setCatelogs(catelogVos);

//      =============== 以上从聚合信息中获取 =================

        //  5、分页信息 - 页码
        result.setPageNum(param.getPageNum());
        //  5、分页信息 - 总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //  5、分页信息 - 总页码，计算
        int totalPages = (int)total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int)total / EsConstant.PRODUCT_PAGESIZE : ((int)total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);

        return result;
    }
}
