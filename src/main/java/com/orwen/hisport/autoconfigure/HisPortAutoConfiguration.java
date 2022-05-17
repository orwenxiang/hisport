package com.orwen.hisport.autoconfigure;

import com.orwen.hisport.artemis.dbaccess.ArtemisDepartPO;
import com.orwen.hisport.common.dbaccess.repository.DBAccessRepositoryImpl;
import com.orwen.hisport.defs.HxPortDefs;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@EnableAsync
@Configuration
@EnableScheduling
@EnableTransactionManagement
@EnableConfigurationProperties(HisPortProperties.class)
@EnableJpaRepositories(basePackages = "com.orwen.hisport", repositoryBaseClass = DBAccessRepositoryImpl.class)
public class HisPortAutoConfiguration {
    @Autowired
    private HisPortProperties properties;

    @Autowired
    private RedissonClient redissonClient;

    @Bean(destroyMethod = "shutdown", name = "patientPullExecutor")
    public ExecutorService patientPullExecutor() {
        return new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 4, 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @Bean(name = "patientPullWeights")
    public RMap<String, Integer> patientPullWeights() {
        return redissonClient.getLocalCachedMap("patient_pull_weights",
                LocalCachedMapOptions.defaults());
    }

    @Bean(name = "patientPullerTopic")
    public RTopic patientPullerTopic() {
        return redissonClient.getTopic(HxPortDefs.PATIENT_PULLER_TOPIC);
    }

    @Bean(name = "patientPullRate")
    public RRateLimiter patientPullRate() {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("patient_pull_rates");
        rateLimiter.setRate(RateType.OVERALL, properties.getPull().getRateInSecond(),
                1, RateIntervalUnit.SECONDS);
        return rateLimiter;
    }

    @Bean("patientPullerRestTemplate")
    @ConditionalOnMissingBean(name = "patientPullerRestTemplate")
    public RestTemplate patientPullerRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        StringHttpMessageConverter messageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        messageConverter.setSupportedMediaTypes(List.
                of(new MediaType("text", "xml", StandardCharsets.UTF_8),
                        new MediaType("text", "html", StandardCharsets.UTF_8)));

        restTemplate.setMessageConverters(List.of(messageConverter));

        return restTemplate;
    }

    @Bean("artemisDepartCache")
    public RLocalCachedMap<String, ArtemisDepartPO> artemisDepartCache() {
        return redissonClient.getLocalCachedMap("artemis_depart_cache",
                LocalCachedMapOptions.<String, ArtemisDepartPO>defaults().evictionPolicy(LocalCachedMapOptions.EvictionPolicy.SOFT));
    }

    @Bean("artemisRetryTemplate")
    @ConditionalOnProperty(prefix = "hisport.artemis.retry", value = "enabled", matchIfMissing = true)
    public RetryTemplate artemisRetryTemplate() {
        RabbitProperties.Retry retry = properties.getArtemis().getRetry();
        PropertyMapper map = PropertyMapper.get();
        RetryTemplate template = new RetryTemplate();
        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        map.from(retry::getMaxAttempts).to(policy::setMaxAttempts);
        template.setRetryPolicy(policy);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        map.from(retry::getInitialInterval).whenNonNull().as(Duration::toMillis)
                .to(backOffPolicy::setInitialInterval);
        map.from(retry::getMultiplier).to(backOffPolicy::setMultiplier);
        map.from(retry::getMaxInterval).whenNonNull().as(Duration::toMillis).to(backOffPolicy::setMaxInterval);
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }
}
