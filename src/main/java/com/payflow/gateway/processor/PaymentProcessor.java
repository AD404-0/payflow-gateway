package com.payflow.gateway.processor;

import com.payflow.gateway.domain.model.PaymentRequest;
import com.payflow.gateway.domain.model.PaymentResult;
import com.payflow.gateway.service.EncryptionService;
import com.payflow.gateway.service.PaymentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for payment processors implementing the Strategy pattern
 * Each payment method type has its own processor implementation
 */
public abstract class PaymentProcessor {
    
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final PaymentValidator validator;
    protected final EncryptionService encryptionService;
    
    protected PaymentProcessor(PaymentValidator validator, EncryptionService encryptionService) {
        this.validator = validator;
        this.encryptionService = encryptionService;
    }
    
    /**
     * Main method to process a payment - implements template method pattern
     */
    public PaymentResult processPayment(PaymentRequest request) {
        try {
            // Validate request
            PaymentValidator.ValidationResult validationResult = validator.validate(request);
            if (!validationResult.isValid()) {
                logger.warn("Payment validation failed: {}", validationResult.getErrorMessage());
                return PaymentResult.failure(generateTransactionId(), "VALIDATION_ERROR", 
                                           validationResult.getErrorMessage());
            }
            
            // Validate business rules
            PaymentValidator.ValidationResult businessValidation = validator.validateBusinessRules(request);
            if (!businessValidation.isValid()) {
                logger.warn("Business rule validation failed: {}", businessValidation.getErrorMessage());
                return PaymentResult.failure(generateTransactionId(), "BUSINESS_RULE_ERROR", 
                                           businessValidation.getErrorMessage());
            }
            
            // Validate merchant authorization
            PaymentValidator.ValidationResult merchantValidation = validator.validateMerchantAuthorization(request.getMerchantId());
            if (!merchantValidation.isValid()) {
                logger.warn("Merchant authorization failed: {}", merchantValidation.getErrorMessage());
                return PaymentResult.failure(generateTransactionId(), "MERCHANT_AUTH_ERROR", 
                                           merchantValidation.getErrorMessage());
            }
            
            logger.info("Processing payment for merchant: {}, amount: {}, type: {}", 
                       request.getMerchantId(), request.getAmount(), request.getPaymentMethodType());
            
            // Process the specific payment type
            return doProcessPayment(request);
            
        } catch (Exception e) {
            logger.error("Error processing payment", e);
            return PaymentResult.failure(generateTransactionId(), "PROCESSING_ERROR", 
                                       "Internal error processing payment");
        }
    }
    
    /**
     * Abstract method to be implemented by specific payment processors
     */
    protected abstract PaymentResult doProcessPayment(PaymentRequest request);
    
    /**
     * Validates if this processor can handle the given payment method
     */
    public abstract boolean canProcess(PaymentRequest request);
    
    /**
     * Returns the supported payment method types for this processor
     */
    public abstract String getSupportedPaymentMethodType();
    
    /**
     * Refunds a payment
     */
    public abstract PaymentResult refundPayment(String transactionId, String amount, String reason);
    
    /**
     * Voids/cancels a payment
     */
    public abstract PaymentResult voidPayment(String transactionId, String reason);
    
    /**
     * Captures a previously authorized payment
     */
    public abstract PaymentResult capturePayment(String transactionId, String amount);
    
    /**
     * Generates a unique transaction ID
     */
    protected String generateTransactionId() {
        return "txn_" + encryptionService.generateToken(16);
    }
    
    /**
     * Encrypts sensitive payment data
     */
    protected String encryptSensitiveData(String data) {
        return encryptionService.encrypt(data);
    }
    
    /**
     * Logs payment processing for audit trail
     */
    protected void logPaymentActivity(String transactionId, String activity, String details) {
        logger.info("Transaction: {} | Activity: {} | Details: {}", transactionId, activity, details);
    }
}