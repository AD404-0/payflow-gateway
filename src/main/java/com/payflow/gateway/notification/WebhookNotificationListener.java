package com.payflow.gateway.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Webhook notification listener that sends HTTP notifications to merchant endpoints
 * Implements the Observer pattern for webhook delivery
 */
@Component
public class WebhookNotificationListener implements PaymentEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookNotificationListener.class);
    private final RestTemplate restTemplate;
    
    public WebhookNotificationListener() {
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public void onPaymentEvent(PaymentEvent event) {
        logger.info("Processing webhook notification for event: {}", event.getEventId());
        
        // Get webhook URL from event data or merchant configuration
        String webhookUrl = getWebhookUrl(event);
        
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            logger.debug("No webhook URL configured for merchant: {}", event.getMerchantId());
            return;
        }
        
        try {
            sendWebhook(webhookUrl, event);
            logger.info("Webhook sent successfully for event: {} to URL: {}", 
                       event.getEventId(), webhookUrl);
        } catch (Exception e) {
            logger.error("Failed to send webhook for event: {} to URL: {}", 
                        event.getEventId(), webhookUrl, e);
            throw e;
        }
    }
    
    @Override
    public String[] getSupportedEventTypes() {
        return new String[]{
            "payment.completed",
            "payment.failed",
            "refund.processed",
            "chargeback.received",
            "transaction.status_changed"
        };
    }
    
    @Override
    public String getListenerName() {
        return "WebhookNotificationListener";
    }
    
    @Override
    public int getPriority() {
        return 10; // High priority for webhook delivery
    }
    
    @Override
    public boolean isAsynchronous() {
        return true; // Webhooks should be sent asynchronously
    }
    
    @Override
    public void onError(PaymentEvent event, Exception error) {
        logger.error("Webhook delivery failed for event: {}, will retry later", event.getEventId());
        // In a real implementation, you would queue this for retry
        // For now, just log the error
    }
    
    private void sendWebhook(String webhookUrl, PaymentEvent event) {
        HttpHeaders headers = createWebhookHeaders(event);
        Map<String, Object> payload = createWebhookPayload(event);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        
        try {
            restTemplate.exchange(webhookUrl, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            logger.error("HTTP error sending webhook to {}: {}", webhookUrl, e.getMessage());
            throw new WebhookDeliveryException("Failed to deliver webhook", e);
        }
    }
    
    private HttpHeaders createWebhookHeaders(PaymentEvent event) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "PayFlow-Webhook/1.0");
        headers.set("X-PayFlow-Event-ID", event.getEventId());
        headers.set("X-PayFlow-Event-Type", event.getEventType());
        headers.set("X-PayFlow-Timestamp", event.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // In a real implementation, you would add HMAC signature for security
        // headers.set("X-PayFlow-Signature", calculateSignature(payload));
        
        return headers;
    }
    
    private Map<String, Object> createWebhookPayload(PaymentEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event_id", event.getEventId());
        payload.put("event_type", event.getEventType());
        payload.put("timestamp", event.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        payload.put("merchant_id", event.getMerchantId());
        
        // Transaction data
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("transaction_id", event.getTransactionId());
        transactionData.put("status", event.getStatus().getCode());
        if (event.getPreviousStatus() != null) {
            transactionData.put("previous_status", event.getPreviousStatus().getCode());
        }
        
        // Add event-specific data
        if (event.getEventData() != null && !event.getEventData().isEmpty()) {
            transactionData.putAll(event.getEventData());
        }
        
        payload.put("transaction", transactionData);
        
        return payload;
    }
    
    private String getWebhookUrl(PaymentEvent event) {
        // In a real implementation, this would look up the merchant's webhook URL
        // from the database or configuration
        String webhookUrl = event.getEventDataAsString("webhook_url");
        
        if (webhookUrl == null) {
            // Mock webhook URL for testing
            webhookUrl = "https://merchant-" + event.getMerchantId() + ".example.com/webhooks/payflow";
        }
        
        return webhookUrl;
    }
    
    /**
     * Exception thrown when webhook delivery fails
     */
    public static class WebhookDeliveryException extends RuntimeException {
        public WebhookDeliveryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}