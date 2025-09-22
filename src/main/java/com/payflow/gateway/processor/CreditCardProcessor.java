package com.payflow.gateway.processor;

import com.payflow.gateway.domain.enums.PaymentMethodType;
import com.payflow.gateway.domain.model.*;
import com.payflow.gateway.service.EncryptionService;
import com.payflow.gateway.service.PaymentValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Credit card payment processor implementation
 * Handles credit card and debit card payments
 */
@Component
public class CreditCardProcessor extends PaymentProcessor {
    
    private final Random random = new Random();
    
    public CreditCardProcessor(PaymentValidator validator, EncryptionService encryptionService) {
        super(validator, encryptionService);
    }
    
    @Override
    protected PaymentResult doProcessPayment(PaymentRequest request) {
        String transactionId = generateTransactionId();
        CreditCardDetails cardDetails = (CreditCardDetails) request.getPaymentMethodDetails();
        
        try {
            logPaymentActivity(transactionId, "CARD_PROCESSING_STARTED", 
                             "Processing credit card payment for " + cardDetails.getMaskedDetails());
            
            // Simulate card validation
            if (!validateCard(cardDetails)) {
                return PaymentResult.failure(transactionId, "INVALID_CARD", "Invalid card details");
            }
            
            // Simulate fraud check
            if (isFraudulent(request)) {
                return PaymentResult.failure(transactionId, "FRAUD_DETECTED", "Transaction flagged as fraudulent");
            }
            
            // Simulate payment processing with external gateway
            PaymentResult result = simulateCardProcessing(transactionId, request, cardDetails);
            
            logPaymentActivity(transactionId, "CARD_PROCESSING_COMPLETED", 
                             "Result: " + result.getStatus() + ", Auth Code: " + result.getAuthorizationCode());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing credit card payment", e);
            return PaymentResult.failure(transactionId, "PROCESSING_ERROR", "Card processing failed");
        }
    }
    
    @Override
    public boolean canProcess(PaymentRequest request) {
        return request.getPaymentMethodType() == PaymentMethodType.CREDIT_CARD ||
               request.getPaymentMethodType() == PaymentMethodType.DEBIT_CARD;
    }
    
    @Override
    public String getSupportedPaymentMethodType() {
        return "credit_card,debit_card";
    }
    
    @Override
    public PaymentResult refundPayment(String transactionId, String amount, String reason) {
        logger.info("Processing refund for transaction: {}, amount: {}", transactionId, amount);
        
        // Simulate refund processing
        if (random.nextDouble() > 0.1) { // 90% success rate
            String refundId = "rfnd_" + encryptionService.generateToken(12);
            PaymentResult result = PaymentResult.success(refundId, "REFUND_" + random.nextInt(100000));
            result.setProcessorResponse("Refund processed successfully");
            logPaymentActivity(transactionId, "REFUND_PROCESSED", "Refund ID: " + refundId);
            return result;
        } else {
            return PaymentResult.failure(transactionId, "REFUND_FAILED", "Unable to process refund");
        }
    }
    
    @Override
    public PaymentResult voidPayment(String transactionId, String reason) {
        logger.info("Voiding transaction: {}, reason: {}", transactionId, reason);
        
        // Simulate void processing
        if (random.nextDouble() > 0.05) { // 95% success rate
            PaymentResult result = PaymentResult.success(transactionId, "VOID_" + random.nextInt(100000));
            result.setProcessorResponse("Transaction voided successfully");
            logPaymentActivity(transactionId, "TRANSACTION_VOIDED", reason);
            return result;
        } else {
            return PaymentResult.failure(transactionId, "VOID_FAILED", "Unable to void transaction");
        }
    }
    
    @Override
    public PaymentResult capturePayment(String transactionId, String amount) {
        logger.info("Capturing payment for transaction: {}, amount: {}", transactionId, amount);
        
        // Simulate capture processing
        if (random.nextDouble() > 0.05) { // 95% success rate
            PaymentResult result = PaymentResult.success(transactionId, "CAPTURE_" + random.nextInt(100000));
            result.setProcessorResponse("Payment captured successfully");
            logPaymentActivity(transactionId, "PAYMENT_CAPTURED", "Amount: " + amount);
            return result;
        } else {
            return PaymentResult.failure(transactionId, "CAPTURE_FAILED", "Unable to capture payment");
        }
    }
    
    private boolean validateCard(CreditCardDetails cardDetails) {
        // Simulate card validation
        return cardDetails.isValid() && !isCardBlacklisted(cardDetails.getCardNumber());
    }
    
    private boolean isCardBlacklisted(String cardNumber) {
        // Simulate blacklist check - block cards ending in 0000
        return cardNumber != null && cardNumber.endsWith("0000");
    }
    
    private boolean isFraudulent(PaymentRequest request) {
        // Simple fraud detection simulation
        // Flag transactions over $5000 as potentially fraudulent
        return request.getAmount().compareTo(new java.math.BigDecimal("5000.00")) > 0 && 
               random.nextDouble() < 0.1; // 10% chance for high-value transactions
    }
    
    private PaymentResult simulateCardProcessing(String transactionId, PaymentRequest request, 
                                               CreditCardDetails cardDetails) {
        // Simulate external payment gateway processing
        try {
            Thread.sleep(100 + random.nextInt(200)); // Simulate network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate different response scenarios
        double outcome = random.nextDouble();
        
        if (outcome < 0.85) { // 85% success rate
            PaymentResult result = PaymentResult.success(transactionId, "AUTH_" + random.nextInt(1000000));
            result.setMerchantId(request.getMerchantId());
            result.setReferenceId(request.getReferenceId());
            result.setAmount(request.getAmount());
            result.setCurrency(request.getCurrency().getCode());
            result.setPaymentMethodType(request.getPaymentMethodType().getCode());
            result.setProcessorResponse("Transaction approved");
            result.setProcessorTransactionId("proc_" + random.nextInt(1000000));
            return result;
        } else if (outcome < 0.95) { // 10% declined
            return PaymentResult.failure(transactionId, "DECLINED", "Card declined by issuer");
        } else { // 5% processing error
            return PaymentResult.failure(transactionId, "GATEWAY_ERROR", "Payment gateway temporarily unavailable");
        }
    }
}