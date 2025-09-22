package com.payflow.gateway.processor;

import com.payflow.gateway.domain.enums.PaymentMethodType;
import com.payflow.gateway.domain.model.*;
import com.payflow.gateway.service.EncryptionService;
import com.payflow.gateway.service.PaymentValidator;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Digital wallet payment processor implementation
 * Handles PayPal, Apple Pay, Google Pay, and other digital wallets
 */
@Component
public class DigitalWalletProcessor extends PaymentProcessor {
    
    private final Random random = new Random();
    
    public DigitalWalletProcessor(PaymentValidator validator, EncryptionService encryptionService) {
        super(validator, encryptionService);
    }
    
    @Override
    protected PaymentResult doProcessPayment(PaymentRequest request) {
        String transactionId = generateTransactionId();
        DigitalWalletDetails walletDetails = (DigitalWalletDetails) request.getPaymentMethodDetails();
        
        try {
            logPaymentActivity(transactionId, "WALLET_PROCESSING_STARTED", 
                             "Processing digital wallet payment for " + walletDetails.getMaskedDetails());
            
            // Validate wallet credentials
            if (!validateWallet(walletDetails)) {
                return PaymentResult.failure(transactionId, "INVALID_WALLET", "Invalid wallet credentials");
            }
            
            // Check wallet status and balance
            if (!isWalletActive(walletDetails)) {
                return PaymentResult.failure(transactionId, "WALLET_INACTIVE", "Digital wallet is inactive or suspended");
            }
            
            // Simulate wallet-specific processing
            PaymentResult result = simulateWalletProcessing(transactionId, request, walletDetails);
            
            logPaymentActivity(transactionId, "WALLET_PROCESSING_COMPLETED", 
                             "Result: " + result.getStatus() + ", Provider: " + walletDetails.getWalletProvider());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing digital wallet payment", e);
            return PaymentResult.failure(transactionId, "PROCESSING_ERROR", "Digital wallet processing failed");
        }
    }
    
    @Override
    public boolean canProcess(PaymentRequest request) {
        return request.getPaymentMethodType() == PaymentMethodType.DIGITAL_WALLET;
    }
    
    @Override
    public String getSupportedPaymentMethodType() {
        return "digital_wallet";
    }
    
    @Override
    public PaymentResult refundPayment(String transactionId, String amount, String reason) {
        logger.info("Processing digital wallet refund for transaction: {}, amount: {}", transactionId, amount);
        
        // Digital wallets typically process refunds quickly
        if (random.nextDouble() > 0.05) { // 95% success rate
            String refundId = "wref_" + encryptionService.generateToken(12);
            PaymentResult result = PaymentResult.success(refundId, "WALLET_REFUND_" + random.nextInt(100000));
            result.setProcessorResponse("Refund processed to digital wallet");
            logPaymentActivity(transactionId, "WALLET_REFUND_PROCESSED", "Refund ID: " + refundId);
            return result;
        } else {
            return PaymentResult.failure(transactionId, "REFUND_FAILED", "Unable to process wallet refund");
        }
    }
    
    @Override
    public PaymentResult voidPayment(String transactionId, String reason) {
        logger.info("Voiding digital wallet transaction: {}, reason: {}", transactionId, reason);
        
        // Digital wallets typically allow voids with high success rate
        if (random.nextDouble() > 0.02) { // 98% success rate
            PaymentResult result = PaymentResult.success(transactionId, "VOID_" + random.nextInt(100000));
            result.setProcessorResponse("Digital wallet transaction voided");
            logPaymentActivity(transactionId, "WALLET_TRANSACTION_VOIDED", reason);
            return result;
        } else {
            return PaymentResult.failure(transactionId, "VOID_FAILED", "Unable to void wallet transaction");
        }
    }
    
    @Override
    public PaymentResult capturePayment(String transactionId, String amount) {
        logger.info("Capturing digital wallet payment for transaction: {}, amount: {}", transactionId, amount);
        
        // Most digital wallets auto-capture, but some support manual capture
        if (random.nextDouble() > 0.1) { // 90% success rate
            PaymentResult result = PaymentResult.success(transactionId, "CAPTURE_" + random.nextInt(100000));
            result.setProcessorResponse("Digital wallet payment captured");
            logPaymentActivity(transactionId, "WALLET_PAYMENT_CAPTURED", "Amount: " + amount);
            return result;
        } else {
            return PaymentResult.failure(transactionId, "CAPTURE_FAILED", "Unable to capture wallet payment");
        }
    }
    
    private boolean validateWallet(DigitalWalletDetails walletDetails) {
        // Simulate wallet validation
        return walletDetails.isValid() && 
               isSupportedWalletProvider(walletDetails.getWalletProvider()) &&
               !isWalletBlacklisted(walletDetails.getWalletId());
    }
    
    private boolean isSupportedWalletProvider(String provider) {
        // Check if we support this wallet provider
        return provider != null && (
            provider.equalsIgnoreCase("PAYPAL") ||
            provider.equalsIgnoreCase("APPLE_PAY") ||
            provider.equalsIgnoreCase("GOOGLE_PAY") ||
            provider.equalsIgnoreCase("SAMSUNG_PAY") ||
            provider.equalsIgnoreCase("VENMO")
        );
    }
    
    private boolean isWalletBlacklisted(String walletId) {
        // Simulate wallet blacklist check
        return walletId != null && walletId.contains("BLOCKED");
    }
    
    private boolean isWalletActive(DigitalWalletDetails walletDetails) {
        // Simulate wallet status check
        return !walletDetails.getWalletId().endsWith("SUSPENDED");
    }
    
    private boolean hasInsufficientFunds(DigitalWalletDetails walletDetails) {
        // Simulate balance check
        return walletDetails.getWalletId().contains("EMPTY");
    }
    
    private PaymentResult simulateWalletProcessing(String transactionId, PaymentRequest request, 
                                                 DigitalWalletDetails walletDetails) {
        // Simulate wallet processing time (usually faster than cards/banks)
        try {
            Thread.sleep(50 + random.nextInt(100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check for insufficient funds
        if (hasInsufficientFunds(walletDetails)) {
            return PaymentResult.failure(transactionId, "INSUFFICIENT_FUNDS", "Insufficient balance in digital wallet");
        }
        
        // Simulate different response scenarios based on wallet provider
        double outcome = random.nextDouble();
        String provider = walletDetails.getWalletProvider().toLowerCase();
        
        // Different success rates by provider
        double successRate = switch (provider) {
            case "paypal" -> 0.92;
            case "apple_pay" -> 0.95;
            case "google_pay" -> 0.94;
            case "samsung_pay" -> 0.90;
            default -> 0.88;
        };
        
        if (outcome < successRate) {
            PaymentResult result = PaymentResult.success(transactionId, 
                                                       provider.toUpperCase() + "_" + random.nextInt(1000000));
            result.setMerchantId(request.getMerchantId());
            result.setReferenceId(request.getReferenceId());
            result.setAmount(request.getAmount());
            result.setCurrency(request.getCurrency().getCode());
            result.setPaymentMethodType(request.getPaymentMethodType().getCode());
            result.setProcessorResponse("Payment processed via " + walletDetails.getWalletProvider());
            result.setProcessorTransactionId("wallet_" + random.nextInt(1000000));
            return result;
        } else if (outcome < 0.98) {
            return PaymentResult.failure(transactionId, "WALLET_DECLINED", "Payment declined by digital wallet");
        } else {
            return PaymentResult.failure(transactionId, "WALLET_ERROR", "Digital wallet service temporarily unavailable");
        }
    }
}