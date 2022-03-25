package com.orwen.hisport.autoconfigure.redisson;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "spring.redis.redisson.pool")
public class RedissonPoolProperties {
    private boolean enabled = true;
    private PoolConfig subscriber = new PoolConfig(1, 32);
    private PoolConfig connection = new PoolConfig(4, 64);

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PoolConfig {
        private int minIdle;
        private int maxActive;
    }
}
