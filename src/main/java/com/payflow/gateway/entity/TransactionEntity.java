package com.payflow.gateway.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.payflow.gateway.domain.enums.Currency;
import com.payflow.gateway.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a payment transaction
 * Maps to the transactions table in PostgreSQL
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_reference_id", columnList = "reference_id"),
    @Index(name = "idx_transaction_created_at", columnList = "created_at"),
    @Index(name = "idx_transaction_merchant_status", columnList = "merchant_id, status")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TransactionEntity {
    
    @Id
    @Column(name = "id", length = 64)
    private String id;
    
    @NotNull
    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;
    
    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;
    
    @Column(name = "reference_id", length = 100, unique = true)
    private String referenceId;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "authorization_code", length = 50)
    private String authorizationCode;
    
    @Column(name = "processor_transaction_id", length = 100)
    private String processorTransactionId;
    
    @Column(name = "processor_response", columnDefinition = "TEXT")
    private String processorResponse;
    
    @Column(name = "error_code", length = 50)
    private String errorCode;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @Column(name = "customer_email", length = 255)
    private String customerEmail;
    
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;
    
    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;
    
    @Column(name = "callback_url", length = 500)
    private String callbackUrl;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", referencedColumnName = "id", insertable = false, updatable = false)
    private MerchantEntity merchant;
    
    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PaymentMethodEntity paymentMethod;
    
    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BillingAddressEntity billingAddress;
    
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionEventEntity> events = new ArrayList<>();
    
    @OneToMany(mappedBy = "originalTransaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RefundEntity> refunds = new ArrayList<>();
    
    // Constructors
    public TransactionEntity() {}
    
    public TransactionEntity(String id, String merchantId, BigDecimal amount, Currency currency) {
        this.id = id;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.status = TransactionStatus.PENDING;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
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
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public String getReferenceId() {
        return referenceId;
    }
    
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAuthorizationCode() {
        return authorizationCode;
    }
    
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
    
    public String getProcessorTransactionId() {
        return processorTransactionId;
    }
    
    public void setProcessorTransactionId(String processorTransactionId) {
        this.processorTransactionId = processorTransactionId;
    }
    
    public String getProcessorResponse() {
        return processorResponse;
    }
    
    public void setProcessorResponse(String processorResponse) {
        this.processorResponse = processorResponse;
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
    
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }
    
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    public MerchantEntity getMerchant() {
        return merchant;
    }
    
    public void setMerchant(MerchantEntity merchant) {
        this.merchant = merchant;
    }
    
    public PaymentMethodEntity getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethodEntity paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public BillingAddressEntity getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(BillingAddressEntity billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public List<TransactionEventEntity> getEvents() {
        return events;
    }
    
    public void setEvents(List<TransactionEventEntity> events) {
        this.events = events;
    }
    
    public List<RefundEntity> getRefunds() {
        return refunds;
    }
    
    public void setRefunds(List<RefundEntity> refunds) {
        this.refunds = refunds;
    }
    
    // Utility methods
    public void addEvent(TransactionEventEntity event) {
        events.add(event);
        event.setTransaction(this);
    }
    
    public void addRefund(RefundEntity refund) {
        refunds.add(refund);
        refund.setOriginalTransaction(this);
    }
    
    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }
    
    public boolean canBeRefunded() {
        return status == TransactionStatus.COMPLETED || status == TransactionStatus.SETTLED;
    }
}