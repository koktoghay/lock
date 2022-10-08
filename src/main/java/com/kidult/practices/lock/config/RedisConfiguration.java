package com.kidult.practices.lock.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author Created by tommy on 2022/10/08.
 */
@Configuration
public class RedisConfiguration {

    private String prefix = "spring.redis.lettuce.pool.";

    @Autowired
    private Environment environment;

    @Bean
    public RedisTemplate<String, ?> redisTemplate() {
        RedisTemplate<String, ?> redisTemplate = new RedisTemplate();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.setConnectionFactory(lettuceConnectionFactory());
        return redisTemplate;
    }

    @Bean
    public GenericObjectPoolConfig redisPool() {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(environment.getProperty(prefix + "max-idle", Integer.class));
        genericObjectPoolConfig.setMaxTotal(environment.getProperty(prefix + "max-active", Integer.class));
        genericObjectPoolConfig.setMaxWait(Duration.ofMillis(environment.getProperty(prefix + "max-wait", Integer.class)));
        genericObjectPoolConfig.setMinIdle(environment.getProperty(prefix + "min-idle", Integer.class));
        return genericObjectPoolConfig;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.redis")
    @Primary
    public RedisStandaloneConfiguration redisConf() {
        return new RedisStandaloneConfiguration();
    }

    @Bean
    @Primary
    public LettuceConnectionFactory lettuceConnectionFactory() {
        GenericObjectPoolConfig redisPool = redisPool();
        LettuceClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder()
                .poolConfig(redisPool).commandTimeout(redisPool.getMaxWaitDuration()).build();
        return new LettuceConnectionFactory(redisConf(), clientConfiguration);
    }
}
