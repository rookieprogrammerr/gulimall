package com.zc.gulimall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author hl
 * @Data 2020/7/16
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zc.gulimall.product.feign"})
public class ApplicationProduct11111 {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationProduct11111.class, args);
    }
}
