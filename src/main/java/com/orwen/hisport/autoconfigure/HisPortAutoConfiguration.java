package com.orwen.hisport.autoconfigure;

import com.orwen.hisport.artemis.dbaccess.ArtemisDepartPO;
import com.orwen.hisport.common.dbaccess.repository.DBAccessRepositoryImpl;
import com.orwen.hisport.defs.HxPortDefs;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

@EnableAsync
@Configuration
@EnableScheduling
@EnableTransactionManagement
@EnableConfigurationProperties(HisPortProperties.class)
@EnableJpaRepositories(basePackages = "com.orwen.hisport", repositoryBaseClass = DBAccessRepositoryImpl.class)
public class HisPortAutoConfiguration {
    @Autowired
    private RedissonClient redissonClient;

    @Bean(destroyMethod = "shutdown", name = "pullHxHisExecutor")
    public ExecutorService pullHxHisExecutor() {
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    @Bean(name = "pullHxHisWeights")
    public RMap<String, Integer> pullHxHisWeights() {
        return redissonClient.getLocalCachedMap("pull_hx_his_weights",
                LocalCachedMapOptions.defaults());
    }

    @Bean(name = "patientPullerTopic")
    public RTopic patientPullerTopic() {
        return redissonClient.getTopic(HxPortDefs.PATIENT_PULLER_TOPIC);
    }

    @Bean("artemisDepartCache")
    public RLocalCachedMap<String, ArtemisDepartPO> artemisDepartCache() {
        return redissonClient.getLocalCachedMap("artemis_depart_cache",
                LocalCachedMapOptions.<String, ArtemisDepartPO>defaults().evictionPolicy(LocalCachedMapOptions.EvictionPolicy.SOFT));
    }
}
