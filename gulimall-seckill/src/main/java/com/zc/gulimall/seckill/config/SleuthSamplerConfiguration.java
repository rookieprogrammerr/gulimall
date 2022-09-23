package com.zc.gulimall.seckill.config;

import brave.sampler.Sampler;
import org.springframework.cloud.sleuth.sampler.ProbabilityBasedSampler;
import org.springframework.cloud.sleuth.sampler.SamplerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 解决引入zipkin与sleuth后项目死锁
 */
@Configuration
public class SleuthSamplerConfiguration {

    @Bean
    public Sampler defaultSampler() throws Exception {
        // 这个值是采样率，设置为1就是100%采样，每一条请求都采，0.1就是10%采样率
        Float f = new Float(0.1f);
        SamplerProperties samplerProperties = new SamplerProperties();
        samplerProperties.setProbability(f);
        ProbabilityBasedSampler sampler = new ProbabilityBasedSampler(samplerProperties);
        return sampler;
    }
}
