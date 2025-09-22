package com.payflow.gateway.security;

import com.payflow.gateway.entity.MerchantEntity;
import com.payflow.gateway.service.MerchantService;
import com.payflow.gateway.service.RedisCacheService;
import com.payflow.gateway.service.RedisRateLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API Key Authentication Service for PayFlow
 * Provides secure API key validation and rate limiting
 */
@Service
public class ApiKeyAuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthenticationService.class);
    
    private final MerchantService merchantService;
    private final Map<String, RateLimitInfo> rateLimitCache = new ConcurrentHashMap<>();
    
    // Redis services (optional - fallback to in-memory if Redis not available)
    @Autowired(required = false)
    private RedisRateLimitService redisRateLimitService;
    
    @Autowired(required = false)
    private RedisCacheService redisCacheService;
    
    // Rate limiting constants
    private static final int MAX_REQUESTS_PER_MINUTE = 1000;
    private static final int MAX_REQUESTS_PER_HOUR = 10000;
    
    public ApiKeyAuthenticationService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }
    
    /**
     * Authenticate and validate API key
     */
    public AuthenticationResult authenticate(String apiKey, String requestPath, String clientIp) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return AuthenticationResult.failure("API key is required", "MISSING_API_KEY");
        }
        
        try {
            // Validate API key format
            if (!isValidApiKeyFormat(apiKey)) {
                return AuthenticationResult.failure("Invalid API key format", "INVALID_API_KEY_FORMAT");
            }
            
            // Try to get merchant from cache first
            MerchantEntity merchant = null;
            if (redisCacheService != null) {
                RedisCacheService.ApiKeyInfo cachedInfo = redisCacheService.getCachedApiKey(apiKey);
                if (cachedInfo != null && cachedInfo.isValid()) {
                    Optional<MerchantEntity> cachedMerchant = merchantService.getMerchant(cachedInfo.getMerchantId());
                    if (cachedMerchant.isPresent()) {
                        merchant = cachedMerchant.get();
                        logger.debug("Using cached merchant for API key: {}", maskApiKey(apiKey));
                    }
                }
            }
            
            // If not in cache, look up merchant by API key
            if (merchant == null) {
                Optional<MerchantEntity> merchantOpt = merchantService.authenticateByApiKey(apiKey);
                if (merchantOpt.isEmpty()) {
                    // Cache the invalid result
                    if (redisCacheService != null) {
                        redisCacheService.cacheApiKey(apiKey, null, false);
                    }
                    logger.warn("Authentication failed for API key: {} from IP: {}", maskApiKey(apiKey), clientIp);
                    return AuthenticationResult.failure("Invalid API key", "INVALID_API_KEY");
                }
                merchant = merchantOpt.get();
                
                // Cache the valid result
                if (redisCacheService != null) {
                    redisCacheService.cacheApiKey(apiKey, merchant.getId(), true);
                }
            }
            
            // Check if merchant is active
            if (!merchant.isActive()) {
                logger.warn("Authentication failed - inactive merchant: {} from IP: {}", merchant.getId(), clientIp);
                return AuthenticationResult.failure("Merchant account is inactive", "INACTIVE_MERCHANT");
            }
            
            // Check rate limits using Redis if available, otherwise fallback to in-memory
            RateLimitResult rateLimitResult;
            if (redisRateLimitService != null) {
                RedisRateLimitService.RateLimitResult redisResult = redisRateLimitService.checkRateLimit(apiKey, clientIp);
                rateLimitResult = new RateLimitResult(
                    redisResult.isAllowed(),
                    redisResult.getRemainingMinute(),
                    redisResult.getRemainingHour(),
                    redisResult.getRetryAfterSeconds()
                );
            } else {
                rateLimitResult = checkRateLimit(apiKey, clientIp);
            }
            
            if (!rateLimitResult.isAllowed()) {
                logger.warn("Rate limit exceeded for merchant: {} from IP: {}", merchant.getId(), clientIp);
                return AuthenticationResult.rateLimitExceeded(rateLimitResult.getRetryAfterSeconds());
            }
            
            // Log successful authentication
            logger.debug("Successful authentication for merchant: {} from IP: {} path: {}", 
                        merchant.getId(), clientIp, requestPath);
            
            return AuthenticationResult.success(merchant, rateLimitResult);
            
        } catch (Exception e) {
            logger.error("Error during API key authentication from IP: {}", clientIp, e);
            return AuthenticationResult.failure("Authentication error", "AUTHENTICATION_ERROR");
        }
    }
    
    /**
     * Validate webhook signature for secure webhook delivery
     */
    public boolean validateWebhookSignature(String payload, String signature, String webhookSecret) {
        if (signature == null || webhookSecret == null) {
            return false;
        }
        
        try {
            // Remove 'sha256=' prefix if present
            String actualSignature = signature.startsWith("sha256=") ? signature.substring(7) : signature;
            
            // Calculate expected signature
            String expectedSignature = calculateHmacSha256(payload, webhookSecret);
            
            // Use constant-time comparison to prevent timing attacks
            return constantTimeEquals(actualSignature, expectedSignature);
            
        } catch (Exception e) {
            logger.error("Error validating webhook signature", e);
            return false;
        }
    }
    
    /**
     * Generate secure API key
     */
    public String generateApiKey(String prefix) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return prefix + "_" + randomPart;
    }
    
    /**
     * Generate secure API secret
     */
    public String generateApiSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * Invalidate API key (for security incidents)
     */
    public void invalidateApiKey(String apiKey, String reason) {
        logger.warn("API key invalidated: {} reason: {}", maskApiKey(apiKey), reason);
        // In a real implementation, this would mark the key as invalid in the database
        // For now, we'll just log it
    }
    
    /**
     * Get rate limit info for monitoring
     */
    public RateLimitInfo getRateLimitInfo(String apiKey) {
        return rateLimitCache.get(apiKey);
    }
    
    /**
     * Clear rate limit cache (for admin purposes)
     */
    public void clearRateLimitCache() {
        rateLimitCache.clear();
        logger.info("Rate limit cache cleared");
    }
    
    // Private helper methods
    
    private boolean isValidApiKeyFormat(String apiKey) {
        // API keys should start with 'pk_' for public keys or 'sk_' for secret keys
        return apiKey.matches("^(pk_|sk_)[A-Za-z0-9_-]{40,}$");
    }
    
    private RateLimitResult checkRateLimit(String apiKey, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        RateLimitInfo rateLimitInfo = rateLimitCache.computeIfAbsent(apiKey, k -> new RateLimitInfo());
        
        // Clean up old entries
        rateLimitInfo.cleanupOldEntries(now);
        
        // Check minute limit
        long requestsInLastMinute = rateLimitInfo.getRequestsInLastMinute(now);
        if (requestsInLastMinute >= MAX_REQUESTS_PER_MINUTE) {
            return RateLimitResult.denied(60); // Retry after 1 minute
        }
        
        // Check hour limit
        long requestsInLastHour = rateLimitInfo.getRequestsInLastHour(now);
        if (requestsInLastHour >= MAX_REQUESTS_PER_HOUR) {
            return RateLimitResult.denied(3600); // Retry after 1 hour
        }
        
        // Record this request
        rateLimitInfo.recordRequest(now);
        
        return RateLimitResult.allowed(
            MAX_REQUESTS_PER_MINUTE - requestsInLastMinute - 1,
            MAX_REQUESTS_PER_HOUR - requestsInLastHour - 1
        );
    }
    
    private String calculateHmacSha256(String data, String secret) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((secret + data).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
    
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }
    
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
    
    /**
     * Authentication result
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final String errorMessage;
        private final String errorCode;
        private final MerchantEntity merchant;
        private final RateLimitResult rateLimitResult;
        private final boolean rateLimited;
        private final int retryAfterSeconds;
        
        private AuthenticationResult(boolean success, String errorMessage, String errorCode,
                                   MerchantEntity merchant, RateLimitResult rateLimitResult,
                                   boolean rateLimited, int retryAfterSeconds) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.errorCode = errorCode;
            this.merchant = merchant;
            this.rateLimitResult = rateLimitResult;
            this.rateLimited = rateLimited;
            this.retryAfterSeconds = retryAfterSeconds;
        }
        
        public static AuthenticationResult success(MerchantEntity merchant, RateLimitResult rateLimitResult) {
            return new AuthenticationResult(true, null, null, merchant, rateLimitResult, false, 0);
        }
        
        public static AuthenticationResult failure(String errorMessage, String errorCode) {
            return new AuthenticationResult(false, errorMessage, errorCode, null, null, false, 0);
        }
        
        public static AuthenticationResult rateLimitExceeded(int retryAfterSeconds) {
            return new AuthenticationResult(false, "Rate limit exceeded", "RATE_LIMIT_EXCEEDED", 
                                          null, null, true, retryAfterSeconds);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public String getErrorCode() { return errorCode; }
        public MerchantEntity getMerchant() { return merchant; }
        public RateLimitResult getRateLimitResult() { return rateLimitResult; }
        public boolean isRateLimited() { return rateLimited; }
        public int getRetryAfterSeconds() { return retryAfterSeconds; }
    }
    
    /**
     * Rate limit result
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final long remainingMinute;
        private final long remainingHour;
        private final int retryAfterSeconds;
        
        private RateLimitResult(boolean allowed, long remainingMinute, long remainingHour, int retryAfterSeconds) {
            this.allowed = allowed;
            this.remainingMinute = remainingMinute;
            this.remainingHour = remainingHour;
            this.retryAfterSeconds = retryAfterSeconds;
        }
        
        public static RateLimitResult allowed(long remainingMinute, long remainingHour) {
            return new RateLimitResult(true, remainingMinute, remainingHour, 0);
        }
        
        public static RateLimitResult denied(int retryAfterSeconds) {
            return new RateLimitResult(false, 0, 0, retryAfterSeconds);
        }
        
        // Getters
        public boolean isAllowed() { return allowed; }
        public long getRemainingMinute() { return remainingMinute; }
        public long getRemainingHour() { return remainingHour; }
        public int getRetryAfterSeconds() { return retryAfterSeconds; }
    }
}