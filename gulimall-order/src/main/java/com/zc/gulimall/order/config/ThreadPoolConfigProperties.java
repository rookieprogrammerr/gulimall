package com.zc.gulimall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
@RefreshScope
public class ThreadPoolConfigProperties {
    private Integer coreSize;   //核心线程数
    private Integer maxSize;    //最大线程数
    private Integer keepAliveTime;  //休眠时长
}
