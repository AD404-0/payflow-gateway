package com.payflow.gateway.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Redis functionality
 * Note: This test requires Redis to be running on localhost:6379
 * Use mvn test -Dtest=RedisConnectionTest to run this specific test
 */
@SpringBootTest
@ActiveProfiles("redis-test")
@TestPropertySource(locations = "classpath:application-redis-test.properties")
public class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private RedisRateLimitService redisRateLimitService;

    @Test
    public void testRedisConnection() {
        // Test basic Redis connectivity
        redisTemplate.opsForValue().set("test-key", "test-value");
        String value = (String) redisTemplate.opsForValue().get("test-key");
        assertEquals("test-value", value);
        
        // Clean up
        redisTemplate.delete("test-key");
    }

    @Test
    public void testRedisConfiguration() {
        // Test Redis configuration
        assertNotNull(redisTemplate);
        assertNotNull(redisCacheService);
        assertNotNull(redisRateLimitService);
        
        // Test JWT blacklist functionality
        String jti = "test-jwt-token-" + System.currentTimeMillis();
        
        // Initially should not be blacklisted
        assertFalse(redisCacheService.isJwtTokenBlacklisted(jti));
        
        // Blacklist the token
        redisCacheService.blacklistJwtToken(jti, Duration.ofMinutes(5));
        
        // Should now be blacklisted
        assertTrue(redisCacheService.isJwtTokenBlacklisted(jti));
    }

    @Test
    public void testRateLimiting() {
        // Test rate limiting functionality
        String testApiKey = "test-api-key-" + System.currentTimeMillis();
        String testIp = "192.168.1.100";
        
        // Get initial rate limit status
        RedisRateLimitService.RateLimitStatus initialStatus = 
            redisRateLimitService.getRateLimitStatus(testApiKey, testIp);
        assertNotNull(initialStatus);
        
        // Check rate limit (should be allowed)
        RedisRateLimitService.RateLimitResult result = 
            redisRateLimitService.checkRateLimit(testApiKey, testIp);
        assertNotNull(result);
        assertTrue(result.isAllowed());
        
        // Get updated status
        RedisRateLimitService.RateLimitStatus updatedStatus = 
            redisRateLimitService.getRateLimitStatus(testApiKey, testIp);
        assertEquals(1, updatedStatus.getMinuteCount());
        
        // Clear rate limit
        redisRateLimitService.clearRateLimit(testApiKey, testIp);
    }

    @Test
    public void testSessionManagement() {
        String sessionId = "test-session-" + System.currentTimeMillis();
        String sessionData = "test-session-data";
        
        // Initially should not exist
        assertNull(redisCacheService.getSession(sessionId));
        
        // Store session
        redisCacheService.storeSession(sessionId, sessionData, Duration.ofMinutes(10));
        
        // Should exist now
        assertEquals(sessionData, redisCacheService.getSession(sessionId));
        
        // Remove session
        redisCacheService.removeSession(sessionId);
        
        // Should no longer exist
        assertNull(redisCacheService.getSession(sessionId));
    }

    @Test
    public void testAnalyticsStorage() {
        String analyticsKey = "test-analytics-" + System.currentTimeMillis();
        String analyticsData = "test-analytics-data";
        
        // Initially should not exist
        assertNull(redisCacheService.getAnalytics(analyticsKey));
        
        // Store analytics data
        redisCacheService.storeAnalytics(analyticsKey, analyticsData, Duration.ofMinutes(5));
        
        // Should exist now
        assertEquals(analyticsData, redisCacheService.getAnalytics(analyticsKey));
    }

    @Test
    public void testCacheStatistics() {
        // Get cache statistics
        RedisCacheService.CacheStats stats = redisCacheService.getCacheStats();
        assertNotNull(stats);
        assertTrue(stats.getTotalKeys() >= 0);
        assertTrue(stats.getMerchantKeys() >= 0);
        assertTrue(stats.getApiKeyKeys() >= 0);
        assertTrue(stats.getTransactionKeys() >= 0);
    }

    @Test
    public void testRedisHealth() {
        // Test Redis health by performing basic operations
        String healthKey = "redis-health-check";
        String healthValue = "ok";
        
        // Set value
        redisTemplate.opsForValue().set(healthKey, healthValue);
        
        // Get value
        String retrievedValue = (String) redisTemplate.opsForValue().get(healthKey);
        assertEquals(healthValue, retrievedValue);
        
        // Check key exists
        assertTrue(redisTemplate.hasKey(healthKey));
        
        // Delete key
        redisTemplate.delete(healthKey);
        
        // Should no longer exist
        assertFalse(redisTemplate.hasKey(healthKey));
    }
}