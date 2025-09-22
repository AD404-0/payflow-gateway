package com.payflow.gateway.service;

import com.payflow.gateway.entity.MerchantEntity;
import com.payflow.gateway.entity.TransactionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis caching service for PayFlow Gateway
 * Provides caching for merchants, API keys, transactions, and other data
 */
@Service
@ConditionalOnProperty(name = "spring.data.redis.host")
@CacheConfig(cacheManager = "cacheManager")
public class RedisCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        logger.info("Redis caching service initialized");
    }

    // =================== MERCHANT CACHING ===================

    /**
     * Cache merchant data by ID
     */
    @Cacheable(value = "merchants", key = "#merchantId")
    public MerchantEntity cacheMerchant(String merchantId, MerchantEntity merchant) {
        logger.debug("Caching merchant: {}", merchantId);
        return merchant;
    }

    /**
     * Get cached merchant
     */
    @Cacheable(value = "merchants", key = "#merchantId")
    public MerchantEntity getCachedMerchant(String merchantId) {
        // This will return null if not in cache, triggering cache miss
        return null;
    }

    /**
     * Update merchant cache
     */
    @CachePut(value = "merchants", key = "#merchantId")
    public MerchantEntity updateMerchantCache(String merchantId, MerchantEntity merchant) {
        logger.debug("Updating merchant cache: {}", merchantId);
        return merchant;
    }

    /**
     * Remove merchant from cache
     */
    @CacheEvict(value = "merchants", key = "#merchantId")
    public void evictMerchant(String merchantId) {
        logger.debug("Evicting merchant from cache: {}", merchantId);
    }

    // =================== API KEY CACHING ===================

    /**
     * Cache API key validation result
     */
    @Cacheable(value = "apiKeys", key = "#apiKey", unless = "#result == null")
    public ApiKeyInfo cacheApiKey(String apiKey, String merchantId, boolean isValid) {
        logger.debug("Caching API key validation: {}", maskApiKey(apiKey));
        return new ApiKeyInfo(merchantId, isValid, LocalDateTime.now());
    }

    /**
     * Get cached API key info
     */
    @Cacheable(value = "apiKeys", key = "#apiKey", unless = "#result == null")
    public ApiKeyInfo getCachedApiKey(String apiKey) {
        return null; // Cache miss returns null
    }

    /**
     * Invalidate API key cache
     */
    @CacheEvict(value = "apiKeys", key = "#apiKey")
    public void evictApiKey(String apiKey) {
        logger.debug("Evicting API key from cache: {}", maskApiKey(apiKey));
    }

    // =================== TRANSACTION CACHING ===================

    /**
     * Cache transaction data
     */
    @Cacheable(value = "transactions", key = "#transactionId")
    public TransactionEntity cacheTransaction(String transactionId, TransactionEntity transaction) {
        logger.debug("Caching transaction: {}", transactionId);
        return transaction;
    }

    /**
     * Update transaction cache
     */
    @CachePut(value = "transactions", key = "#transactionId")
    public TransactionEntity updateTransactionCache(String transactionId, TransactionEntity transaction) {
        logger.debug("Updating transaction cache: {}", transactionId);
        return transaction;
    }

    /**
     * Remove transaction from cache
     */
    @CacheEvict(value = "transactions", key = "#transactionId")
    public void evictTransaction(String transactionId) {
        logger.debug("Evicting transaction from cache: {}", transactionId);
    }

    // =================== JWT BLACKLIST ===================

    /**
     * Add JWT token to blacklist
     */
    public void blacklistJwtToken(String jti, Duration ttl) {
        try {
            String key = "jwt_blacklist:" + jti;
            redisTemplate.opsForValue().set(key, "blacklisted", ttl.getSeconds(), TimeUnit.SECONDS);
            logger.debug("JWT token blacklisted: {}", jti);
        } catch (Exception e) {
            logger.error("Error blacklisting JWT token: {}", jti, e);
        }
    }

    /**
     * Check if JWT token is blacklisted
     */
    public boolean isJwtTokenBlacklisted(String jti) {
        try {
            String key = "jwt_blacklist:" + jti;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            logger.error("Error checking JWT blacklist for token: {}", jti, e);
            return false; // On error, allow the token
        }
    }

    // =================== SESSION MANAGEMENT ===================

    /**
     * Store session data
     */
    public void storeSession(String sessionId, Object sessionData, Duration ttl) {
        try {
            String key = "session:" + sessionId;
            redisTemplate.opsForValue().set(key, sessionData, ttl.getSeconds(), TimeUnit.SECONDS);
            logger.debug("Session stored: {}", sessionId);
        } catch (Exception e) {
            logger.error("Error storing session: {}", sessionId, e);
        }
    }

    /**
     * Get session data
     */
    public Object getSession(String sessionId) {
        try {
            String key = "session:" + sessionId;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Error retrieving session: {}", sessionId, e);
            return null;
        }
    }

    /**
     * Remove session
     */
    public void removeSession(String sessionId) {
        try {
            String key = "session:" + sessionId;
            redisTemplate.delete(key);
            logger.debug("Session removed: {}", sessionId);
        } catch (Exception e) {
            logger.error("Error removing session: {}", sessionId, e);
        }
    }

    // =================== ANALYTICS CACHING ===================

    /**
     * Store analytics data with TTL
     */
    public void storeAnalytics(String key, Object data, Duration ttl) {
        try {
            String analyticsKey = "analytics:" + key;
            redisTemplate.opsForValue().set(analyticsKey, data, ttl.getSeconds(), TimeUnit.SECONDS);
            logger.debug("Analytics data stored: {}", key);
        } catch (Exception e) {
            logger.error("Error storing analytics data: {}", key, e);
        }
    }

    /**
     * Get analytics data
     */
    public Object getAnalytics(String key) {
        try {
            String analyticsKey = "analytics:" + key;
            return redisTemplate.opsForValue().get(analyticsKey);
        } catch (Exception e) {
            logger.error("Error retrieving analytics data: {}", key, e);
            return null;
        }
    }

    // =================== CACHE MANAGEMENT ===================

    /**
     * Clear all caches
     */
    @CacheEvict(value = {"merchants", "apiKeys", "transactions"}, allEntries = true)
    public void clearAllCaches() {
        logger.info("All caches cleared");
    }

    /**
     * Get cache statistics
     */
    public CacheStats getCacheStats() {
        try {
            Set<String> allKeys = redisTemplate.keys("*");
            long totalKeys = allKeys != null ? allKeys.size() : 0;
            
            long merchantKeys = countKeysWithPrefix("merchants::");
            long apiKeyKeys = countKeysWithPrefix("apiKeys::");
            long transactionKeys = countKeysWithPrefix("transactions::");
            long rateLimitKeys = countKeysWithPrefix("rate_limit:");
            long sessionKeys = countKeysWithPrefix("session:");
            long analyticsKeys = countKeysWithPrefix("analytics:");
            
            return new CacheStats(totalKeys, merchantKeys, apiKeyKeys, transactionKeys, 
                                rateLimitKeys, sessionKeys, analyticsKeys);
        } catch (Exception e) {
            logger.error("Error getting cache statistics", e);
            return new CacheStats(0, 0, 0, 0, 0, 0, 0);
        }
    }

    private long countKeysWithPrefix(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    // =================== DATA CLASSES ===================

    /**
     * API Key cache information
     */
    public static class ApiKeyInfo {
        private String merchantId;
        private boolean isValid;
        private LocalDateTime cachedAt;

        // Default constructor for Jackson deserialization
        public ApiKeyInfo() {
        }

        public ApiKeyInfo(String merchantId, boolean isValid, LocalDateTime cachedAt) {
            this.merchantId = merchantId;
            this.isValid = isValid;
            this.cachedAt = cachedAt;
        }

        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { this.isValid = valid; } // For Jackson compatibility
        public void setIsValid(boolean isValid) { this.isValid = isValid; }
        
        public LocalDateTime getCachedAt() { return cachedAt; }
        public void setCachedAt(LocalDateTime cachedAt) { this.cachedAt = cachedAt; }
    }

    /**
     * Cache statistics
     */
    public static class CacheStats {
        private final long totalKeys;
        private final long merchantKeys;
        private final long apiKeyKeys;
        private final long transactionKeys;
        private final long rateLimitKeys;
        private final long sessionKeys;
        private final long analyticsKeys;

        public CacheStats(long totalKeys, long merchantKeys, long apiKeyKeys, long transactionKeys,
                         long rateLimitKeys, long sessionKeys, long analyticsKeys) {
            this.totalKeys = totalKeys;
            this.merchantKeys = merchantKeys;
            this.apiKeyKeys = apiKeyKeys;
            this.transactionKeys = transactionKeys;
            this.rateLimitKeys = rateLimitKeys;
            this.sessionKeys = sessionKeys;
            this.analyticsKeys = analyticsKeys;
        }

        // Getters
        public long getTotalKeys() { return totalKeys; }
        public long getMerchantKeys() { return merchantKeys; }
        public long getApiKeyKeys() { return apiKeyKeys; }
        public long getTransactionKeys() { return transactionKeys; }
        public long getRateLimitKeys() { return rateLimitKeys; }
        public long getSessionKeys() { return sessionKeys; }
        public long getAnalyticsKeys() { return analyticsKeys; }
    }
}