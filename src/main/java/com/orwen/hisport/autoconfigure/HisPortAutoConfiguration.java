package com.orwen.hisport.autoconfigure;

import com.orwen.hisport.artemis.dbaccess.ArtemisDepartPO;
import com.orwen.hisport.common.dbaccess.repository.DBAccessRepositoryImpl;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(HisPortProperties.class)
@AutoConfigureAfter(RedissonNodeAutoConfiguration.class)
@EnableJpaRepositories(basePackages = "com.orwen.hisport", repositoryBaseClass = DBAccessRepositoryImpl.class)
public class HisPortAutoConfiguration {
    @Autowired
    private RedissonClient redissonClient;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService pullHxHisExecutor() {
        return redissonClient.getExecutorService("hx_his_patient_pull_executor");
    }

    @Bean("artemisDepartCache")
    public RLocalCachedMap<String, ArtemisDepartPO> artemisDepartCache() {
        return redissonClient.getLocalCachedMap("artemis_depart_cache", LocalCachedMapOptions.<String, ArtemisDepartPO>defaults()
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.SOFT));
    }
}
