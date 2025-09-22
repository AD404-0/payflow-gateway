package com.payflow.gateway.notification;

import com.payflow.gateway.domain.enums.TransactionStatus;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event object for payment notifications
 * Contains all information needed for notification delivery
 */
public class PaymentEvent {
    
    private final String eventId;
    private final String eventType;
    private final String transactionId;
    private final String merchantId;
    private final TransactionStatus status;
    private final TransactionStatus previousStatus;
    private final LocalDateTime timestamp;
    private final Map<String, Object> eventData;
    private final String source;
    
    public PaymentEvent(String eventType, String transactionId, String merchantId, 
                       TransactionStatus status, TransactionStatus previousStatus, 
                       Map<String, Object> eventData, String source) {
        this.eventId = "evt_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        this.eventType = eventType;
        this.transactionId = transactionId;
        this.merchantId = merchantId;
        this.status = status;
        this.previousStatus = previousStatus;
        this.timestamp = LocalDateTime.now();
        this.eventData = eventData != null ? eventData : Map.of();
        this.source = source;
    }
    
    // Factory methods for common event types
    public static PaymentEvent transactionStatusChanged(String transactionId, String merchantId,
                                                      TransactionStatus newStatus, TransactionStatus oldStatus,
                                                      Map<String, Object> data) {
        return new PaymentEvent("transaction.status_changed", transactionId, merchantId, 
                              newStatus, oldStatus, data, "payment_processor");
    }
    
    public static PaymentEvent paymentCompleted(String transactionId, String merchantId, 
                                              Map<String, Object> data) {
        return new PaymentEvent("payment.completed", transactionId, merchantId, 
                              TransactionStatus.COMPLETED, TransactionStatus.PROCESSING, data, "payment_processor");
    }
    
    public static PaymentEvent paymentFailed(String transactionId, String merchantId, 
                                           Map<String, Object> data) {
        return new PaymentEvent("payment.failed", transactionId, merchantId, 
                              TransactionStatus.FAILED, TransactionStatus.PROCESSING, data, "payment_processor");
    }
    
    public static PaymentEvent refundProcessed(String transactionId, String merchantId, 
                                             Map<String, Object> data) {
        return new PaymentEvent("refund.processed", transactionId, merchantId, 
                              TransactionStatus.REFUNDED, TransactionStatus.COMPLETED, data, "refund_processor");
    }
    
    public static PaymentEvent chargebackReceived(String transactionId, String merchantId, 
                                                Map<String, Object> data) {
        return new PaymentEvent("chargeback.received", transactionId, merchantId, 
                              TransactionStatus.DISPUTED, TransactionStatus.COMPLETED, data, "dispute_processor");
    }
    
    // Getters
    public String getEventId() {
        return eventId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public TransactionStatus getPreviousStatus() {
        return previousStatus;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Object> getEventData() {
        return eventData;
    }
    
    public String getSource() {
        return source;
    }
    
    // Utility methods
    public Object getEventData(String key) {
        return eventData.get(key);
    }
    
    public String getEventDataAsString(String key) {
        Object value = eventData.get(key);
        return value != null ? value.toString() : null;
    }
    
    public boolean isStatusChange() {
        return previousStatus != null && !previousStatus.equals(status);
    }
    
    @Override
    public String toString() {
        return String.format("PaymentEvent[id=%s, type=%s, txn=%s, merchant=%s, status=%s->%s, timestamp=%s]",
                           eventId, eventType, transactionId, merchantId, previousStatus, status, timestamp);
    }
}