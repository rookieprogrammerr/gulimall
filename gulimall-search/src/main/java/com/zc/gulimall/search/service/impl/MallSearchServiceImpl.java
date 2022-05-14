package com.zc.gulimall.search.service.impl;

import com.zc.gulimall.search.config.GulimallElasticSearchConfig;
import com.zc.gulimall.search.constant.EsConstant;
import com.zc.gulimall.search.entity.vo.SearchParam;
import com.zc.gulimall.search.entity.vo.SearchResult;
import com.zc.gulimall.search.service.MallSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;

@Service
@Slf4j
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
            result = buildSearchResult(response);
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
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
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

        String s = sourceBuilder.toString();
        log.info("构建的DSL", s);
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }


    /**
     * 构建结果数据
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response) {

        return null;
    }
}
