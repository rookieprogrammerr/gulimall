package com.zc.gulimall.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author hl
 * @Data 2020/7/22
 */
@Configuration
@EnableTransactionManagement
public class MybatisConfig {
    @Autowired
    private DataSourceProperties dataSourceProperties;

    /**
     * mp 分页插件
     *
     * @return
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面，true为回到首页，false为继续请求
        paginationInterceptor.setOverflow(true);
        // 每页最大为1000条 （默认500）
        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}
