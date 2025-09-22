package com.payflow.gateway.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a merchant
 * Maps to the merchants table in PostgreSQL
 */
@Entity
@Table(name = "merchants", indexes = {
    @Index(name = "idx_merchant_api_key", columnList = "api_key", unique = true),
    @Index(name = "idx_merchant_status", columnList = "status"),
    @Index(name = "idx_merchant_email", columnList = "email", unique = true)
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MerchantEntity {
    
    @Id
    @Column(name = "id", length = 64)
    private String id;
    
    @NotBlank
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Email
    @NotBlank
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @NotBlank
    @Column(name = "api_key", nullable = false, unique = true, length = 128)
    private String apiKey;
    
    @Column(name = "api_secret", length = 128)
    private String apiSecret;
    
    @NotBlank
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";
    
    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;
    
    @Column(name = "webhook_secret", length = 128)
    private String webhookSecret;
    
    @Column(name = "business_type", length = 50)
    private String businessType;
    
    @Column(name = "country", length = 3)
    private String country;
    
    @Column(name = "timezone", length = 50)
    private String timezone;
    
    @Column(name = "is_test_mode", nullable = false)
    private Boolean isTestMode = true;
    
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
    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore  // Prevent circular reference in JSON serialization
    private List<TransactionEntity> transactions = new ArrayList<>();
    
    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore  // Prevent circular reference in JSON serialization
    private List<SettlementEntity> settlements = new ArrayList<>();
    
    // Constructors
    public MerchantEntity() {}
    
    public MerchantEntity(String id, String name, String email, String apiKey) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.apiKey = apiKey;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiSecret() {
        return apiSecret;
    }
    
    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
    
    public String getWebhookSecret() {
        return webhookSecret;
    }
    
    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }
    
    public String getBusinessType() {
        return businessType;
    }
    
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public Boolean getIsTestMode() {
        return isTestMode;
    }
    
    public void setIsTestMode(Boolean isTestMode) {
        this.isTestMode = isTestMode;
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
    
    public List<TransactionEntity> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }
    
    public List<SettlementEntity> getSettlements() {
        return settlements;
    }
    
    public void setSettlements(List<SettlementEntity> settlements) {
        this.settlements = settlements;
    }
    
    // Utility methods
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    public boolean isTestMode() {
        return Boolean.TRUE.equals(isTestMode);
    }
}