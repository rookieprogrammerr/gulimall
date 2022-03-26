package com.zc.gulimall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author hl
 * @Data 2020/7/16
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApplicationOrder9000 {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationOrder9000.class, args);
    }
}
