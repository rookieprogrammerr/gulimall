package com.zc.gulimall.coupon;

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
@EnableFeignClients
public class ApplicationCoupon7000 {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationCoupon7000.class, args);
    }
}
