package com.zc.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1、导入依赖
 * 2、编写配置，给容器中注入一个 RestHighLevelClient
 * 3、参照官方API
 */
@Configuration
public class GulimallElasticSearchConfig {

    @Value("${spring.elasticsearch.rest.uris}")
    private String uris;

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Auth", "Bearer");
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient ESRestClient() {

        RestClientBuilder builder = null;

        String[] url = uris.split(":");

        builder = RestClient.builder(new HttpHost(url[0], Integer.parseInt(url[1]), "http"));

        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }

}
