package com.cym.distributed.transaction.core.config;

import com.cym.distributed.transaction.core.utils.FastJson2JsonRedisSerializer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 19:45
 * @description:
 */
@Configuration
@ComponentScan("com.cym.distributed.transaction.core")
public class CommonConfig {

    /**
     * 使用fastjson 序列化存储value
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // FastJson2JsonRedisSerializer 替换默认序列化
        FastJson2JsonRedisSerializer<Object> fastJson2JsonRedisSerializer = new FastJson2JsonRedisSerializer<>(Object.class);

        // 设置value的序列化规则和 key的序列化规则
        redisTemplate.setValueSerializer(fastJson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(fastJson2JsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(redisTemplate.getKeySerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
