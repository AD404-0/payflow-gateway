package com.payflow.gateway.domain.model;

import com.payflow.gateway.domain.enums.TransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents the result of a payment processing operation
 */
public class PaymentResult {
    
    private String transactionId;
    private String merchantId;
    private String referenceId;
    private TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private String paymentMethodType;
    private String processorResponse;
    private String processorTransactionId;
    private String authorizationCode;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime processedAt;
    private Map<String, String> metadata;
    private boolean success;
    
    // Constructors
    public PaymentResult() {}
    
    public PaymentResult(String transactionId, TransactionStatus status, boolean success) {
        this.transactionId = transactionId;
        this.status = status;
        this.success = success;
        this.processedAt = LocalDateTime.now();
    }
    
    // Static factory methods
    public static PaymentResult success(String transactionId, String authorizationCode) {
        PaymentResult result = new PaymentResult(transactionId, TransactionStatus.COMPLETED, true);
        result.setAuthorizationCode(authorizationCode);
        return result;
    }
    
    public static PaymentResult pending(String transactionId) {
        return new PaymentResult(transactionId, TransactionStatus.PENDING, false);
    }
    
    public static PaymentResult processing(String transactionId) {
        return new PaymentResult(transactionId, TransactionStatus.PROCESSING, false);
    }
    
    public static PaymentResult failure(String transactionId, String errorCode, String errorMessage) {
        PaymentResult result = new PaymentResult(transactionId, TransactionStatus.FAILED, false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }
    
    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public String getReferenceId() {
        return referenceId;
    }
    
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getPaymentMethodType() {
        return paymentMethodType;
    }
    
    public void setPaymentMethodType(String paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }
    
    public String getProcessorResponse() {
        return processorResponse;
    }
    
    public void setProcessorResponse(String processorResponse) {
        this.processorResponse = processorResponse;
    }
    
    public String getProcessorTransactionId() {
        return processorTransactionId;
    }
    
    public void setProcessorTransactionId(String processorTransactionId) {
        this.processorTransactionId = processorTransactionId;
    }
    
    public String getAuthorizationCode() {
        return authorizationCode;
    }
    
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}