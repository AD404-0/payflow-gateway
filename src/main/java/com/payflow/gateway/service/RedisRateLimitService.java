package com.payflow.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based rate limiting service
 * Provides distributed rate limiting using Redis atomic operations
 */
@Service
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisRateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RedisRateLimitService.class);

    private final RedisTemplate<String, String> rateLimitRedisTemplate;

    // Rate limiting constants
    private static final int MAX_REQUESTS_PER_MINUTE = 1000;
    private static final int MAX_REQUESTS_PER_HOUR = 10000;
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final String MINUTE_SUFFIX = ":minute";
    private static final String HOUR_SUFFIX = ":hour";

    // Lua script for atomic rate limiting
    private static final String RATE_LIMIT_SCRIPT = 
        "local current = redis.call('GET', KEYS[1]) " +
        "if current == false then " +
        "  redis.call('SET', KEYS[1], 1) " +
        "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
        "  return {1, ARGV[2] - 1} " +
        "end " +
        "local count = tonumber(current) " +
        "if count < tonumber(ARGV[2]) then " +
        "  redis.call('INCR', KEYS[1]) " +
        "  return {count + 1, ARGV[2] - count - 1} " +
        "else " +
        "  return {count, 0} " +
        "end";

    private final DefaultRedisScript<List> rateLimitLuaScript;

    public RedisRateLimitService(RedisTemplate<String, String> rateLimitRedisTemplate) {
        this.rateLimitRedisTemplate = rateLimitRedisTemplate;
        this.rateLimitLuaScript = new DefaultRedisScript<>();
        this.rateLimitLuaScript.setScriptText(RATE_LIMIT_SCRIPT);
        this.rateLimitLuaScript.setResultType(List.class);
        
        logger.info("Redis rate limiting service initialized");
    }

    /**
     * Check if request is allowed and update counters atomically
     */
    public RateLimitResult checkRateLimit(String apiKey, String clientIp) {
        String identifier = apiKey != null ? apiKey : clientIp;
        LocalDateTime now = LocalDateTime.now();
        
        try {
            // Check minute rate limit
            String minuteKey = RATE_LIMIT_KEY_PREFIX + identifier + MINUTE_SUFFIX + ":" + 
                              now.getYear() + ":" + now.getDayOfYear() + ":" + now.getHour() + ":" + now.getMinute();
            
            List<Long> minuteResult = rateLimitRedisTemplate.execute(rateLimitLuaScript, 
                Collections.singletonList(minuteKey), "60", String.valueOf(MAX_REQUESTS_PER_MINUTE));
            
            long minuteCount = minuteResult.get(0);
            long minuteRemaining = minuteResult.get(1);
            
            if (minuteRemaining <= 0) {
                logger.warn("Minute rate limit exceeded for identifier: {} (count: {})", identifier, minuteCount);
                return RateLimitResult.denied(60, minuteCount, 0, MAX_REQUESTS_PER_HOUR);
            }
            
            // Check hour rate limit
            String hourKey = RATE_LIMIT_KEY_PREFIX + identifier + HOUR_SUFFIX + ":" + 
                            now.getYear() + ":" + now.getDayOfYear() + ":" + now.getHour();
            
            List<Long> hourResult = rateLimitRedisTemplate.execute(rateLimitLuaScript, 
                Collections.singletonList(hourKey), "3600", String.valueOf(MAX_REQUESTS_PER_HOUR));
            
            long hourCount = hourResult.get(0);
            long hourRemaining = hourResult.get(1);
            
            if (hourRemaining <= 0) {
                logger.warn("Hour rate limit exceeded for identifier: {} (count: {})", identifier, hourCount);
                return RateLimitResult.denied(3600, minuteRemaining, hourCount, 0);
            }
            
            logger.debug("Rate limit check passed for identifier: {} (minute: {}/{}, hour: {}/{})", 
                        identifier, minuteCount, MAX_REQUESTS_PER_MINUTE, hourCount, MAX_REQUESTS_PER_HOUR);
            
            return RateLimitResult.allowed(minuteRemaining, hourRemaining);
            
        } catch (Exception e) {
            logger.error("Error checking rate limit for identifier: {}", identifier, e);
            // On Redis error, allow the request but log the issue
            return RateLimitResult.allowed(MAX_REQUESTS_PER_MINUTE - 1, MAX_REQUESTS_PER_HOUR - 1);
        }
    }

    /**
     * Get current rate limit status for monitoring
     */
    public RateLimitStatus getRateLimitStatus(String apiKey, String clientIp) {
        String identifier = apiKey != null ? apiKey : clientIp;
        LocalDateTime now = LocalDateTime.now();
        
        try {
            String minuteKey = RATE_LIMIT_KEY_PREFIX + identifier + MINUTE_SUFFIX + ":" + 
                              now.getYear() + ":" + now.getDayOfYear() + ":" + now.getHour() + ":" + now.getMinute();
            String hourKey = RATE_LIMIT_KEY_PREFIX + identifier + HOUR_SUFFIX + ":" + 
                            now.getYear() + ":" + now.getDayOfYear() + ":" + now.getHour();
            
            String minuteCountStr = rateLimitRedisTemplate.opsForValue().get(minuteKey);
            String hourCountStr = rateLimitRedisTemplate.opsForValue().get(hourKey);
            
            long minuteCount = minuteCountStr != null ? Long.parseLong(minuteCountStr) : 0;
            long hourCount = hourCountStr != null ? Long.parseLong(hourCountStr) : 0;
            
            return new RateLimitStatus(
                identifier,
                minuteCount,
                MAX_REQUESTS_PER_MINUTE - minuteCount,
                hourCount,
                MAX_REQUESTS_PER_HOUR - hourCount,
                rateLimitRedisTemplate.getExpire(minuteKey, TimeUnit.SECONDS),
                rateLimitRedisTemplate.getExpire(hourKey, TimeUnit.SECONDS)
            );
            
        } catch (Exception e) {
            logger.error("Error getting rate limit status for identifier: {}", identifier, e);
            return new RateLimitStatus(identifier, 0, MAX_REQUESTS_PER_MINUTE, 0, MAX_REQUESTS_PER_HOUR, 60, 3600);
        }
    }

    /**
     * Clear rate limits for a specific identifier (admin function)
     */
    public void clearRateLimit(String apiKey, String clientIp) {
        String identifier = apiKey != null ? apiKey : clientIp;
        LocalDateTime now = LocalDateTime.now();
        
        try {
            String minuteKey = RATE_LIMIT_KEY_PREFIX + identifier + MINUTE_SUFFIX + ":" + 
                              now.getYear() + ":" + now.getDayOfYear() + ":" + now.getHour() + ":" + now.getMinute();
            String hourKey = RATE_LIMIT_KEY_PREFIX + identifier + HOUR_SUFFIX + ":" + 
                            now.getYear() + ":" + now.getDayOfYear() + ":" + now.getHour();
            
            rateLimitRedisTemplate.delete(minuteKey);
            rateLimitRedisTemplate.delete(hourKey);
            
            logger.info("Cleared rate limits for identifier: {}", identifier);
            
        } catch (Exception e) {
            logger.error("Error clearing rate limit for identifier: {}", identifier, e);
        }
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

        public static RateLimitResult denied(int retryAfterSeconds, long remainingMinute, long remainingHour, long otherRemaining) {
            return new RateLimitResult(false, remainingMinute, remainingHour, retryAfterSeconds);
        }

        // Getters
        public boolean isAllowed() { return allowed; }
        public long getRemainingMinute() { return remainingMinute; }
        public long getRemainingHour() { return remainingHour; }
        public int getRetryAfterSeconds() { return retryAfterSeconds; }
    }

    /**
     * Rate limit status for monitoring
     */
    public static class RateLimitStatus {
        private final String identifier;
        private final long minuteCount;
        private final long minuteRemaining;
        private final long hourCount;
        private final long hourRemaining;
        private final long minuteTtl;
        private final long hourTtl;

        public RateLimitStatus(String identifier, long minuteCount, long minuteRemaining, 
                              long hourCount, long hourRemaining, long minuteTtl, long hourTtl) {
            this.identifier = identifier;
            this.minuteCount = minuteCount;
            this.minuteRemaining = minuteRemaining;
            this.hourCount = hourCount;
            this.hourRemaining = hourRemaining;
            this.minuteTtl = minuteTtl;
            this.hourTtl = hourTtl;
        }

        // Getters
        public String getIdentifier() { return identifier; }
        public long getMinuteCount() { return minuteCount; }
        public long getMinuteRemaining() { return minuteRemaining; }
        public long getHourCount() { return hourCount; }
        public long getHourRemaining() { return hourRemaining; }
        public long getMinuteTtl() { return minuteTtl; }
        public long getHourTtl() { return hourTtl; }
    }
}