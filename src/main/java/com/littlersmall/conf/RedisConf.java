package com.littlersmall.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by littlersmall on 16/5/16.
 */
@Configuration
public class RedisConf {
    @Value("${redis.ip}")
    private String ip;

    @Value("${redis.port}")
    private int port;

    @Bean
    public JedisPoolConfig buildPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        jedisPoolConfig.setMaxTotal(250);
        jedisPoolConfig.setMaxIdle(25);
        jedisPoolConfig.setMaxWaitMillis(5000);

        return jedisPoolConfig;
    }

    @Bean
    public JedisConnectionFactory buildConnectionFactory(JedisPoolConfig jedisPoolConfig) {
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();

        connectionFactory.setHostName(ip);
        connectionFactory.setPort(port);
        connectionFactory.setPoolConfig(jedisPoolConfig);

        return connectionFactory;
    }

    @Bean
    @Scope("prototype")
    public <String, T> RedisTemplate buildRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, T> redisTemplate = new RedisTemplate<String, T>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        return redisTemplate;
    }
}
