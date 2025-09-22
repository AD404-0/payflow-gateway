package com.payflow.gateway.service;

import com.payflow.gateway.domain.model.PaymentRequest;
import com.payflow.gateway.domain.model.PaymentMethodDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for validating payment requests and payment method details
 */
@Service
public class PaymentValidator {
    
    /**
     * Validates a payment request
     */
    public ValidationResult validate(PaymentRequest request) {
        List<String> errors = new ArrayList<>();
        
        // Basic null checks
        if (request == null) {
            errors.add("Payment request cannot be null");
            return new ValidationResult(false, errors);
        }
        
        // Validate merchant ID
        if (request.getMerchantId() == null || request.getMerchantId().trim().isEmpty()) {
            errors.add("Merchant ID is required");
        }
        
        // Validate amount
        if (request.getAmount() == null) {
            errors.add("Amount is required");
        } else if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Amount must be greater than zero");
        } else if (request.getAmount().compareTo(new BigDecimal("999999.99")) > 0) {
            errors.add("Amount exceeds maximum limit");
        }
        
        // Validate currency
        if (request.getCurrency() == null) {
            errors.add("Currency is required");
        }
        
        // Validate payment method type
        if (request.getPaymentMethodType() == null) {
            errors.add("Payment method type is required");
        }
        
        // Validate payment method details
        if (request.getPaymentMethodDetails() == null) {
            errors.add("Payment method details are required");
        } else if (!request.getPaymentMethodDetails().isValid()) {
            errors.add("Invalid payment method details");
        }
        
        // Validate expiration
        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(LocalDateTime.now())) {
            errors.add("Payment request has expired");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validates business rules for the payment
     */
    public ValidationResult validateBusinessRules(PaymentRequest request) {
        List<String> errors = new ArrayList<>();
        
        // Check for duplicate reference ID (would need database check in real implementation)
        if (request.getReferenceId() != null && isDuplicateReference(request.getReferenceId())) {
            errors.add("Duplicate reference ID");
        }
        
        // Validate amount limits per merchant (would need merchant configuration)
        if (request.getAmount() != null && isAmountExceedsLimit(request.getMerchantId(), request.getAmount())) {
            errors.add("Amount exceeds merchant daily limit");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validates if the merchant is authorized to process payments
     */
    public ValidationResult validateMerchantAuthorization(String merchantId) {
        List<String> errors = new ArrayList<>();
        
        // In real implementation, check merchant status in database
        if (!isMerchantActive(merchantId)) {
            errors.add("Merchant is not authorized to process payments");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    // Mock implementations - would be replaced with actual database checks
    private boolean isDuplicateReference(String referenceId) {
        // Mock: always return false for now
        return false;
    }
    
    private boolean isAmountExceedsLimit(String merchantId, BigDecimal amount) {
        // Mock: check against a sample limit
        return amount.compareTo(new BigDecimal("10000.00")) > 0;
    }
    
    private boolean isMerchantActive(String merchantId) {
        // Mock: always return true for now
        return true;
    }
    
    /**
     * Result of validation operation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}