package com.zc.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.sun.javafx.collections.MappingChange;
import com.zc.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    RestHighLevelClient client;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class User {
        private String userNmae;
        private String gender;
        private Integer age;
    }

    /**
     * 测试存储数据到es
     * 更新也可以
     */
    @Test
    void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");

        //第一种：直接写k-v
        /*indexRequest.source(
                "userName", "张三",
                "age", 18,
                "gender","男"
        );*/

        //第二种：传入json
        User user = new User("张三", "男", 18);
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);

        //执行操作
        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Test
    void searchData() throws IOException {
        //1、创建一个检索请求
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        //指定DSL（检索条件）
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //1.1）、构造检索条件
        sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        //sourceBuilder.from();
        //sourceBuilder.size();

        //1.2）、按照年龄的值分布进行聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        sourceBuilder.aggregation(ageAgg);

        //1.3）、计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(balanceAvg);

        System.out.println("检索条件：" + sourceBuilder.toString());
        searchRequest.source(sourceBuilder);

        //2、执行检索
        SearchResponse search = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        //3、分析结果 search
        System.out.println(search.toString());

        //3.1）、获取所有查到的数据
        SearchHits hits = search.getHits();
        SearchHit[] searchHits = hits.getHits();

        for (SearchHit hit : searchHits) {
            //hit.getIndex();
            //hit.getType();
            //hit.getId();
            String string = hit.getSourceAsString();
            Account account = JSON.parseObject(string, Account.class);
            System.out.println("account：" + account);
        }

        //3.2）、获取这次检索到的分析信息
        Aggregations aggregations = search.getAggregations();
        /*for (Aggregation aggregation : aggregations.asList()) {
            System.out.println("当前聚合：" + aggregation.getName());
            aggregation.getMetaData()
        }*/
        Terms agg = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : agg.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄：" + keyAsString + "==>" + bucket.getDocCount());
        }

        Avg balanceAgg = aggregations.get("balanceAgg");
        System.out.println("平均薪资：" + balanceAgg.getValue());
    }

    @Test
    void contextLoads() {
        System.out.println(client);
    }

}
