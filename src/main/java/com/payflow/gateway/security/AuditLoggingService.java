package com.payflow.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Audit logging service for security events and transactions
 * Provides comprehensive logging for compliance and security monitoring
 */
@Service
public class AuditLoggingService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLoggingService.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private static final Logger transactionLogger = LoggerFactory.getLogger("TRANSACTION");
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    private final ObjectMapper objectMapper;
    
    public AuditLoggingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Log authentication events
     */
    public void logAuthentication(String merchantId, String apiKey, String clientIp, 
                                String userAgent, boolean success, String failureReason) {
        AuditEvent event = AuditEvent.builder()
            .eventType("AUTHENTICATION")
            .merchantId(merchantId)
            .clientIp(clientIp)
            .userAgent(userAgent)
            .success(success)
            .add("api_key_masked", maskApiKey(apiKey))
            .add("failure_reason", failureReason)
            .build();
        
        securityLogger.info(formatAuditEvent(event));
    }
    
    /**
     * Log payment processing events
     */
    public void logPaymentProcessing(String transactionId, String merchantId, String amount, 
                                   String currency, String paymentMethod, String status, 
                                   String clientIp, Map<String, Object> additionalData) {
        AuditEvent event = AuditEvent.builder()
            .eventType("PAYMENT_PROCESSING")
            .transactionId(transactionId)
            .merchantId(merchantId)
            .clientIp(clientIp)
            .success("COMPLETED".equals(status))
            .add("amount", amount)
            .add("currency", currency)
            .add("payment_method", paymentMethod)
            .add("status", status)
            .addAll(additionalData)
            .build();
        
        transactionLogger.info(formatAuditEvent(event));
    }
    
    /**
     * Log refund events
     */
    public void logRefund(String transactionId, String originalTransactionId, String merchantId,
                         String amount, String reason, boolean success, String clientIp) {
        AuditEvent event = AuditEvent.builder()
            .eventType("REFUND")
            .transactionId(transactionId)
            .merchantId(merchantId)
            .clientIp(clientIp)
            .success(success)
            .add("original_transaction_id", originalTransactionId)
            .add("refund_amount", amount)
            .add("reason", reason)
            .build();
        
        auditLogger.info(formatAuditEvent(event));
    }
    
    /**
     * Log security incidents
     */
    public void logSecurityIncident(String incidentType, String merchantId, String clientIp,
                                  String description, String severity, Map<String, Object> details) {
        AuditEvent event = AuditEvent.builder()
            .eventType("SECURITY_INCIDENT")
            .merchantId(merchantId)
            .clientIp(clientIp)
            .success(false)
            .add("incident_type", incidentType)
            .add("description", description)
            .add("severity", severity)
            .addAll(details)
            .build();
        
        securityLogger.warn(formatAuditEvent(event));
    }
    
    /**
     * Log rate limiting events
     */
    public void logRateLimit(String merchantId, String apiKey, String clientIp, 
                           String endpoint, long requestCount, String action) {
        AuditEvent event = AuditEvent.builder()
            .eventType("RATE_LIMIT")
            .merchantId(merchantId)
            .clientIp(clientIp)
            .success("ALLOWED".equals(action))
            .add("api_key_masked", maskApiKey(apiKey))
            .add("endpoint", endpoint)
            .add("request_count", requestCount)
            .add("action", action)
            .build();
        
        securityLogger.info(formatAuditEvent(event));
    }
    
    /**
     * Log data access events
     */
    public void logDataAccess(String merchantId, String resource, String action, 
                            String clientIp, boolean success, String details) {
        AuditEvent event = AuditEvent.builder()
            .eventType("DATA_ACCESS")
            .merchantId(merchantId)
            .clientIp(clientIp)
            .success(success)
            .add("resource", resource)
            .add("action", action)
            .add("details", details)
            .build();
        
        auditLogger.info(formatAuditEvent(event));
    }
    
    /**
     * Log webhook events
     */
    public void logWebhookDelivery(String merchantId, String webhookUrl, String eventType,
                                 String transactionId, boolean success, int responseCode, String error) {
        AuditEvent event = AuditEvent.builder()
            .eventType("WEBHOOK_DELIVERY")
            .merchantId(merchantId)
            .transactionId(transactionId)
            .success(success)
            .add("webhook_url", webhookUrl)
            .add("event_type", eventType)
            .add("response_code", responseCode)
            .add("error", error)
            .build();
        
        auditLogger.info(formatAuditEvent(event));
    }
    
    /**
     * Log configuration changes
     */
    public void logConfigurationChange(String merchantId, String configType, String oldValue,
                                     String newValue, String changedBy, String clientIp) {
        AuditEvent event = AuditEvent.builder()
            .eventType("CONFIGURATION_CHANGE")
            .merchantId(merchantId)
            .clientIp(clientIp)
            .success(true)
            .add("config_type", configType)
            .add("old_value", oldValue)
            .add("new_value", newValue)
            .add("changed_by", changedBy)
            .build();
        
        auditLogger.info(formatAuditEvent(event));
    }
    
    // Private helper methods
    
    private String formatAuditEvent(AuditEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            logger.error("Error formatting audit event", e);
            return event.toString();
        }
    }
    
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
    
    /**
     * Audit event data structure
     */
    public static class AuditEvent {
        private final String eventType;
        private final String transactionId;
        private final String merchantId;
        private final String clientIp;
        private final String userAgent;
        private final LocalDateTime timestamp;
        private final boolean success;
        private final Map<String, Object> details;
        
        private AuditEvent(Builder builder) {
            this.eventType = builder.eventType;
            this.transactionId = builder.transactionId;
            this.merchantId = builder.merchantId;
            this.clientIp = builder.clientIp;
            this.userAgent = builder.userAgent;
            this.timestamp = LocalDateTime.now();
            this.success = builder.success;
            this.details = builder.details;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters
        public String getEventType() { return eventType; }
        public String getTransactionId() { return transactionId; }
        public String getMerchantId() { return merchantId; }
        public String getClientIp() { return clientIp; }
        public String getUserAgent() { return userAgent; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isSuccess() { return success; }
        public Map<String, Object> getDetails() { return details; }
        
        public static class Builder {
            private String eventType;
            private String transactionId;
            private String merchantId;
            private String clientIp;
            private String userAgent;
            private boolean success;
            private Map<String, Object> details = new HashMap<>();
            
            public Builder eventType(String eventType) {
                this.eventType = eventType;
                return this;
            }
            
            public Builder transactionId(String transactionId) {
                this.transactionId = transactionId;
                return this;
            }
            
            public Builder merchantId(String merchantId) {
                this.merchantId = merchantId;
                return this;
            }
            
            public Builder clientIp(String clientIp) {
                this.clientIp = clientIp;
                return this;
            }
            
            public Builder userAgent(String userAgent) {
                this.userAgent = userAgent;
                return this;
            }
            
            public Builder success(boolean success) {
                this.success = success;
                return this;
            }
            
            public Builder add(String key, Object value) {
                if (value != null) {
                    this.details.put(key, value);
                }
                return this;
            }
            
            public Builder addAll(Map<String, Object> additionalDetails) {
                if (additionalDetails != null) {
                    this.details.putAll(additionalDetails);
                }
                return this;
            }
            
            public AuditEvent build() {
                return new AuditEvent(this);
            }
        }
        
        @Override
        public String toString() {
            return String.format("AuditEvent{eventType='%s', merchantId='%s', timestamp=%s, success=%s}", 
                               eventType, merchantId, timestamp, success);
        }
    }
}