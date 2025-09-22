package com.payflow.gateway.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis configuration for PayFlow Gateway
 * Provides caching, rate limiting, and session management
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    /**
     * Configure Redis template with JSON serialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        logger.info("Configuring Redis template with JSON serialization");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure JSON serialization
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new JavaTimeModule());
        
        jsonSerializer.setObjectMapper(objectMapper);

        // Use String serialization for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serialization for values
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure Redis cache manager with different TTL for different cache types
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        logger.info("Configuring Redis cache manager");
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(createJsonSerializer()))
                .entryTtl(Duration.ofHours(1)) // Default TTL: 1 hour
                .disableCachingNullValues();

        // Specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        
        // Rate limiting cache - short TTL
        cacheConfigs.put("rateLimits", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        
        // Merchant cache - medium TTL
        cacheConfigs.put("merchants", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // API key cache - medium TTL
        cacheConfigs.put("apiKeys", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Transaction cache - short TTL
        cacheConfigs.put("transactions", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // JWT blacklist - TTL based on token expiration
        cacheConfigs.put("jwtBlacklist", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // Session cache
        cacheConfigs.put("sessions", defaultConfig.entryTtl(Duration.ofHours(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    /**
     * Redis template specifically for rate limiting
     */
    @Bean
    public RedisTemplate<String, String> rateLimitRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serialization for both keys and values for rate limiting
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    private Jackson2JsonRedisSerializer<Object> createJsonSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new JavaTimeModule());
        
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }
}