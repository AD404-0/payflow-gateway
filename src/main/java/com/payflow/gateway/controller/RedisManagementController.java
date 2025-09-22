package com.payflow.gateway.controller;

import com.payflow.gateway.controller.ApiResponse;
import com.payflow.gateway.service.RedisCacheService;
import com.payflow.gateway.service.RedisRateLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis management and monitoring controller
 * Provides endpoints for Redis health checks, cache management, and statistics
 */
@RestController
@RequestMapping("/api/admin/redis")
@ConditionalOnProperty(name = "spring.data.redis.host")
@PreAuthorize("hasRole('ADMIN')")
public class RedisManagementController {

    private static final Logger logger = LoggerFactory.getLogger(RedisManagementController.class);

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private RedisCacheService redisCacheService;

    @Autowired(required = false)
    private RedisRateLimitService redisRateLimitService;

    /**
     * Check Redis health and connectivity
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkRedisHealth() {
        Map<String, Object> healthInfo = new HashMap<>();
        
        try {
            if (redisTemplate == null) {
                healthInfo.put("status", "DISABLED");
                healthInfo.put("message", "Redis is not configured");
                return ResponseEntity.ok(ApiResponse.success(healthInfo, "Redis health check completed"));
            }

            // Test Redis connectivity
            String pingResult = redisTemplate.getConnectionFactory().getConnection().ping();
            boolean isConnected = "PONG".equals(pingResult);
            
            healthInfo.put("status", isConnected ? "UP" : "DOWN");
            healthInfo.put("ping", pingResult);
            healthInfo.put("timestamp", LocalDateTime.now());
            
            if (isConnected) {
                // Get Redis info
                healthInfo.put("connected", true);
                healthInfo.put("cacheService", redisCacheService != null ? "AVAILABLE" : "UNAVAILABLE");
                healthInfo.put("rateLimitService", redisRateLimitService != null ? "AVAILABLE" : "UNAVAILABLE");
            }
            
            logger.info("Redis health check completed: {}", isConnected ? "UP" : "DOWN");
            return ResponseEntity.ok(ApiResponse.success(healthInfo, "Redis health check completed"));
            
        } catch (Exception e) {
            logger.error("Redis health check failed", e);
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
            healthInfo.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(ApiResponse.success(healthInfo, "Redis health check failed"));
        }
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheStats() {
        if (redisCacheService == null) {
            return ResponseEntity.ok(ApiResponse.error("REDIS_UNAVAILABLE", "Redis cache service not available"));
        }

        try {
            RedisCacheService.CacheStats stats = redisCacheService.getCacheStats();
            
            Map<String, Object> statsMap = new HashMap<>();
            statsMap.put("totalKeys", stats.getTotalKeys());
            statsMap.put("merchantKeys", stats.getMerchantKeys());
            statsMap.put("apiKeyKeys", stats.getApiKeyKeys());
            statsMap.put("transactionKeys", stats.getTransactionKeys());
            statsMap.put("rateLimitKeys", stats.getRateLimitKeys());
            statsMap.put("sessionKeys", stats.getSessionKeys());
            statsMap.put("analyticsKeys", stats.getAnalyticsKeys());
            statsMap.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(statsMap, "Cache statistics retrieved"));
            
        } catch (Exception e) {
            logger.error("Error getting cache statistics", e);
            return ResponseEntity.ok(ApiResponse.error("STATS_ERROR", "Error retrieving cache statistics: " + e.getMessage()));
        }
    }

    /**
     * Clear all caches
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<ApiResponse<String>> clearAllCaches() {
        if (redisCacheService == null) {
            return ResponseEntity.ok(ApiResponse.error("REDIS_UNAVAILABLE", "Redis cache service not available"));
        }

        try {
            redisCacheService.clearAllCaches();
            logger.info("All caches cleared by admin request");
            return ResponseEntity.ok(ApiResponse.success("All caches cleared successfully"));
            
        } catch (Exception e) {
            logger.error("Error clearing caches", e);
            return ResponseEntity.ok(ApiResponse.error("CLEAR_ERROR", "Error clearing caches: " + e.getMessage()));
        }
    }

    /**
     * Get rate limit status for a specific API key
     */
    @GetMapping("/rate-limit/{apiKey}")
    public ResponseEntity<ApiResponse<Object>> getRateLimitStatus(@PathVariable String apiKey) {
        if (redisRateLimitService == null) {
            return ResponseEntity.ok(ApiResponse.error("REDIS_UNAVAILABLE", "Redis rate limit service not available"));
        }

        try {
            RedisRateLimitService.RateLimitStatus status = redisRateLimitService.getRateLimitStatus(apiKey, null);
            
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("identifier", status.getIdentifier());
            statusMap.put("minuteCount", status.getMinuteCount());
            statusMap.put("minuteRemaining", status.getMinuteRemaining());
            statusMap.put("hourCount", status.getHourCount());
            statusMap.put("hourRemaining", status.getHourRemaining());
            statusMap.put("minuteTtl", status.getMinuteTtl());
            statusMap.put("hourTtl", status.getHourTtl());
            statusMap.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(statusMap, "Rate limit status retrieved"));
            
        } catch (Exception e) {
            logger.error("Error getting rate limit status for API key: {}", apiKey, e);
            return ResponseEntity.ok(ApiResponse.error("RATE_LIMIT_ERROR", "Error retrieving rate limit status: " + e.getMessage()));
        }
    }

    /**
     * Clear rate limits for a specific API key
     */
    @DeleteMapping("/rate-limit/{apiKey}")
    public ResponseEntity<ApiResponse<String>> clearRateLimit(@PathVariable String apiKey) {
        if (redisRateLimitService == null) {
            return ResponseEntity.ok(ApiResponse.error("REDIS_UNAVAILABLE", "Redis rate limit service not available"));
        }

        try {
            redisRateLimitService.clearRateLimit(apiKey, null);
            logger.info("Rate limits cleared for API key: {} by admin request", maskApiKey(apiKey));
            return ResponseEntity.ok(ApiResponse.success("Rate limits cleared for API key"));
            
        } catch (Exception e) {
            logger.error("Error clearing rate limits for API key: {}", apiKey, e);
            return ResponseEntity.ok(ApiResponse.error("CLEAR_ERROR", "Error clearing rate limits: " + e.getMessage()));
        }
    }

    /**
     * Test Redis connectivity with a ping
     */
    @PostMapping("/ping")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pingRedis() {
        if (redisTemplate == null) {
            return ResponseEntity.ok(ApiResponse.error("REDIS_UNAVAILABLE", "Redis template not available"));
        }

        try {
            long startTime = System.currentTimeMillis();
            String result = redisTemplate.getConnectionFactory().getConnection().ping();
            long endTime = System.currentTimeMillis();
            
            Map<String, Object> pingResult = new HashMap<>();
            pingResult.put("result", result);
            pingResult.put("responseTime", endTime - startTime);
            pingResult.put("timestamp", LocalDateTime.now());
            pingResult.put("status", "PONG".equals(result) ? "SUCCESS" : "FAILED");
            
            return ResponseEntity.ok(ApiResponse.success(pingResult, "Redis ping completed"));
            
        } catch (Exception e) {
            logger.error("Error pinging Redis", e);
            return ResponseEntity.ok(ApiResponse.error("PING_ERROR", "Error pinging Redis: " + e.getMessage()));
        }
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}