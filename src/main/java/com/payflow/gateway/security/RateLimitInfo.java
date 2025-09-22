package com.payflow.gateway.security;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rate limiting information for API key authentication
 * Tracks request timestamps for rate limiting calculations
 */
public class RateLimitInfo {
    
    private final ConcurrentLinkedQueue<LocalDateTime> requestTimestamps = new ConcurrentLinkedQueue<>();
    private volatile LocalDateTime lastCleanup = LocalDateTime.now();
    
    /**
     * Record a new request timestamp
     */
    public void recordRequest(LocalDateTime timestamp) {
        requestTimestamps.offer(timestamp);
    }
    
    /**
     * Get number of requests in the last minute
     */
    public long getRequestsInLastMinute(LocalDateTime now) {
        LocalDateTime oneMinuteAgo = now.minusMinutes(1);
        return requestTimestamps.stream()
            .filter(timestamp -> timestamp.isAfter(oneMinuteAgo))
            .count();
    }
    
    /**
     * Get number of requests in the last hour
     */
    public long getRequestsInLastHour(LocalDateTime now) {
        LocalDateTime oneHourAgo = now.minusHours(1);
        return requestTimestamps.stream()
            .filter(timestamp -> timestamp.isAfter(oneHourAgo))
            .count();
    }
    
    /**
     * Clean up old request timestamps to prevent memory leaks
     */
    public void cleanupOldEntries(LocalDateTime now) {
        // Only cleanup every 5 minutes to avoid excessive processing
        if (lastCleanup.isBefore(now.minusMinutes(5))) {
            LocalDateTime cutoff = now.minusHours(2); // Keep 2 hours of history
            requestTimestamps.removeIf(timestamp -> timestamp.isBefore(cutoff));
            lastCleanup = now;
        }
    }
    
    /**
     * Get total number of recorded requests
     */
    public int getTotalRequests() {
        return requestTimestamps.size();
    }
    
    /**
     * Clear all request history
     */
    public void clear() {
        requestTimestamps.clear();
        lastCleanup = LocalDateTime.now();
    }
}