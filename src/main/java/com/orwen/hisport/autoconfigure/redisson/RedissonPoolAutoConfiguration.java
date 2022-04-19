package com.orwen.hisport.autoconfigure.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.codec.MarshallingCodec;
import org.redisson.config.*;
import org.redisson.connection.MasterSlaveConnectionManager;
import org.redisson.connection.SingleConnectionManager;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedissonAutoConfiguration.class)
@EnableConfigurationProperties(RedissonPoolProperties.class)
@ConditionalOnProperty(prefix = "spring.redis.redisson.pool", name = "enabled", havingValue = "true", matchIfMissing = true)
class RedissonPoolAutoConfiguration implements RedissonAutoConfigurationCustomizer {
    private static final List<String> GET_CONFIG_METHODS = Arrays.asList("getSingleServerConfig",
            "getMasterSlaveServersConfig", "getSentinelServersConfig", "getClusterServersConfig",
            "getReplicatedServersConfig");
    @Autowired
    private RedissonPoolProperties poolProperties;

    @Override
    public void customize(Config config) {
        config.setLockWatchdogTimeout(5 * 1000L);
        if (config.getCodec() == null) {
            config.setCodec(new MarshallingCodec());
        }
        GET_CONFIG_METHODS.stream().map(methodName -> RedissonPoolAutoConfiguration.getConfig(config, methodName))
                .filter(Objects::nonNull)
                .forEach(argConfig -> {
                    if (argConfig instanceof SingleServerConfig) {
                        customize((SingleServerConfig) argConfig, config);
                        return;
                    }
                    if (argConfig instanceof BaseMasterSlaveServersConfig) {
                        customize((BaseMasterSlaveServersConfig) argConfig);
                        return;
                    }
                    log.error("Not support customize pools for type {}", argConfig.getClass());
                });
    }

    protected void customize(SingleServerConfig singleConfig, Config config) {
        RedissonPoolProperties.PoolConfig subscriberConfig = poolProperties.getSubscriber();
        singleConfig.setSubscriptionConnectionMinimumIdleSize(subscriberConfig.getMinIdle());
        singleConfig.setSubscriptionConnectionPoolSize(subscriberConfig.getMaxActive());

        RedissonPoolProperties.PoolConfig connectionConfig = poolProperties.getConnection();
        singleConfig.setConnectionMinimumIdleSize(connectionConfig.getMinIdle());
        singleConfig.setConnectionPoolSize(connectionConfig.getMaxActive());

        config.useCustomServers(new MasterSlaveConnectionManager(buildConfig(singleConfig), config, UUID.randomUUID()));

        Field singleServerConfigField = ReflectionUtils.findField(config.getClass(), "singleServerConfig");
        singleServerConfigField.setAccessible(true);
        ReflectionUtils.setField(singleServerConfigField, config, null);
    }

    protected void customize(BaseMasterSlaveServersConfig baseConfig) {
        RedissonPoolProperties.PoolConfig subscriberConfig = poolProperties.getSubscriber();
        baseConfig.setSubscriptionConnectionMinimumIdleSize(subscriberConfig.getMinIdle());
        baseConfig.setSubscriptionConnectionPoolSize(subscriberConfig.getMaxActive());

        RedissonPoolProperties.PoolConfig connectionConfig = poolProperties.getConnection();
        baseConfig.setMasterConnectionMinimumIdleSize(connectionConfig.getMinIdle());
        baseConfig.setMasterConnectionPoolSize(connectionConfig.getMaxActive());
        baseConfig.setSlaveConnectionMinimumIdleSize(connectionConfig.getMinIdle());
        baseConfig.setSlaveConnectionPoolSize(connectionConfig.getMaxActive());
    }

    private static BaseConfig getConfig(Config config, String methodName) {
        Method method = ReflectionUtils.findMethod(config.getClass(), methodName);
        method.setAccessible(true);
        return (BaseConfig) ReflectionUtils.invokeMethod(method, config);
    }

    private static MasterSlaveServersConfig buildConfig(SingleServerConfig serverConfig) {
        Method originBuildMethod = ReflectionUtils.findMethod(SingleConnectionManager.class, "create", SingleServerConfig.class);
        originBuildMethod.setAccessible(true);
        MasterSlaveServersConfig masterSlaveServersConfig = (MasterSlaveServersConfig) ReflectionUtils.invokeMethod(originBuildMethod, null, serverConfig);
        masterSlaveServersConfig.setSlaveConnectionMinimumIdleSize(0);
        masterSlaveServersConfig.setSlaveConnectionPoolSize(0);
        return masterSlaveServersConfig;
    }
}
