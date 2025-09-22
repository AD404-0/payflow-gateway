package com.payflow.gateway.domain.model;

import com.payflow.gateway.domain.enums.Currency;
import com.payflow.gateway.domain.enums.PaymentMethodType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a payment request in the PayFlow system
 * Contains all necessary information to process a payment
 */
public class PaymentRequest {
    
    @NotNull(message = "Merchant ID is required")
    private String merchantId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Currency is required")
    private Currency currency;
    
    @NotNull(message = "Payment method type is required")
    private PaymentMethodType paymentMethodType;
    
    @NotNull(message = "Payment method details are required")
    private PaymentMethodDetails paymentMethodDetails;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Reference ID can only contain alphanumeric characters, hyphens, and underscores")
    private String referenceId;
    
    private String customerEmail;
    
    private String customerPhone;
    
    private BillingAddress billingAddress;
    
    private Map<String, String> metadata;
    
    private String callbackUrl;
    
    private String webhookUrl;
    
    private LocalDateTime expiresAt;
    
    private boolean captureImmediately = true;
    
    // Constructors
    public PaymentRequest() {}
    
    public PaymentRequest(String merchantId, BigDecimal amount, Currency currency, 
                         PaymentMethodType paymentMethodType, PaymentMethodDetails paymentMethodDetails) {
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethodType = paymentMethodType;
        this.paymentMethodDetails = paymentMethodDetails;
    }
    
    // Getters and Setters
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    
    public PaymentMethodType getPaymentMethodType() {
        return paymentMethodType;
    }
    
    public void setPaymentMethodType(PaymentMethodType paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }
    
    public PaymentMethodDetails getPaymentMethodDetails() {
        return paymentMethodDetails;
    }
    
    public void setPaymentMethodDetails(PaymentMethodDetails paymentMethodDetails) {
        this.paymentMethodDetails = paymentMethodDetails;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getReferenceId() {
        return referenceId;
    }
    
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public BillingAddress getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(BillingAddress billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }
    
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public boolean isCaptureImmediately() {
        return captureImmediately;
    }
    
    public void setCaptureImmediately(boolean captureImmediately) {
        this.captureImmediately = captureImmediately;
    }
}