package com.orwen.hisport.autoconfigure;

import org.redisson.RedissonNode;
import org.redisson.api.RedissonClient;
import org.redisson.config.RedissonNodeConfig;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(RedissonAutoConfiguration.class)
public class RedissonNodeAutoConfiguration {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private BeanFactory beanFactory;

    @Bean(initMethod = "start")
    public RedissonNode redissonNode() {
        RedissonNodeConfig nodeConfig = new RedissonNodeConfig(redissonClient.getConfig());
        nodeConfig.setBeanFactory(beanFactory);
        return RedissonNode.create(nodeConfig, redissonClient);
    }
}
