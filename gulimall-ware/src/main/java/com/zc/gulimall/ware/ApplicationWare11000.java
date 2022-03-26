package com.zc.gulimall.ware;

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
@EnableFeignClients(basePackages = {"com.zc.gulimall.ware.feign"})
public class ApplicationWare11000 {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationWare11000.class, args);
    }
}
