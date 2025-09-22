package com.payflow.gateway.processor;

import com.payflow.gateway.domain.enums.PaymentMethodType;
import com.payflow.gateway.domain.model.*;
import com.payflow.gateway.service.EncryptionService;
import com.payflow.gateway.service.PaymentValidator;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Bank transfer payment processor implementation
 * Handles ACH, wire transfers, and direct bank transfers
 */
@Component
public class BankTransferProcessor extends PaymentProcessor {
    
    private final Random random = new Random();
    
    public BankTransferProcessor(PaymentValidator validator, EncryptionService encryptionService) {
        super(validator, encryptionService);
    }
    
    @Override
    protected PaymentResult doProcessPayment(PaymentRequest request) {
        String transactionId = generateTransactionId();
        BankTransferDetails bankDetails = (BankTransferDetails) request.getPaymentMethodDetails();
        
        try {
            logPaymentActivity(transactionId, "BANK_TRANSFER_STARTED", 
                             "Processing bank transfer for " + bankDetails.getMaskedDetails());
            
            // Validate bank account
            if (!validateBankAccount(bankDetails)) {
                return PaymentResult.failure(transactionId, "INVALID_ACCOUNT", "Invalid bank account details");
            }
            
            // Check account status
            if (!isAccountActive(bankDetails)) {
                return PaymentResult.failure(transactionId, "ACCOUNT_INACTIVE", "Bank account is inactive or closed");
            }
            
            // Simulate bank transfer processing
            PaymentResult result = simulateBankTransfer(transactionId, request, bankDetails);
            
            logPaymentActivity(transactionId, "BANK_TRANSFER_COMPLETED", 
                             "Result: " + result.getStatus() + ", Reference: " + result.getProcessorTransactionId());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing bank transfer", e);
            return PaymentResult.failure(transactionId, "PROCESSING_ERROR", "Bank transfer processing failed");
        }
    }
    
    @Override
    public boolean canProcess(PaymentRequest request) {
        return request.getPaymentMethodType() == PaymentMethodType.BANK_TRANSFER;
    }
    
    @Override
    public String getSupportedPaymentMethodType() {
        return "bank_transfer";
    }
    
    @Override
    public PaymentResult refundPayment(String transactionId, String amount, String reason) {
        logger.info("Processing bank transfer refund for transaction: {}, amount: {}", transactionId, amount);
        
        // Bank transfers typically take 3-5 business days to process refunds
        if (random.nextDouble() > 0.05) { // 95% success rate
            String refundId = "bref_" + encryptionService.generateToken(12);
            PaymentResult result = PaymentResult.pending(refundId);
            result.setProcessorResponse("Refund initiated - will complete in 3-5 business days");
            logPaymentActivity(transactionId, "REFUND_INITIATED", "Refund ID: " + refundId);
            return result;
        } else {
            return PaymentResult.failure(transactionId, "REFUND_FAILED", "Unable to initiate bank transfer refund");
        }
    }
    
    @Override
    public PaymentResult voidPayment(String transactionId, String reason) {
        logger.info("Attempting to void bank transfer: {}, reason: {}", transactionId, reason);
        
        // Bank transfers can only be voided if not yet processed
        if (random.nextDouble() > 0.3) { // 70% can be voided (if caught early)
            PaymentResult result = PaymentResult.success(transactionId, "VOID_" + random.nextInt(100000));
            result.setProcessorResponse("Bank transfer cancelled before processing");
            logPaymentActivity(transactionId, "TRANSFER_VOIDED", reason);
            return result;
        } else {
            return PaymentResult.failure(transactionId, "VOID_FAILED", "Bank transfer already in processing - cannot void");
        }
    }
    
    @Override
    public PaymentResult capturePayment(String transactionId, String amount) {
        logger.info("Bank transfer capture requested for transaction: {}", transactionId);
        
        // Bank transfers are typically auto-captured, so this might not be applicable
        PaymentResult result = PaymentResult.failure(transactionId, "NOT_SUPPORTED", 
                                                   "Manual capture not supported for bank transfers");
        logPaymentActivity(transactionId, "CAPTURE_NOT_SUPPORTED", "Bank transfers are auto-captured");
        return result;
    }
    
    private boolean validateBankAccount(BankTransferDetails bankDetails) {
        // Simulate bank account validation
        return bankDetails.isValid() && 
               isValidRoutingNumber(bankDetails.getRoutingNumber()) &&
               !isBankAccountBlacklisted(bankDetails.getAccountNumber());
    }
    
    private boolean isValidRoutingNumber(String routingNumber) {
        // Simulate routing number validation using check digit algorithm
        if (routingNumber == null || routingNumber.length() != 9) {
            return false;
        }
        
        // Simple checksum validation (real implementation would use proper ABA routing number validation)
        try {
            int checksum = 0;
            int[] weights = {3, 7, 1, 3, 7, 1, 3, 7, 1};
            
            for (int i = 0; i < 9; i++) {
                checksum += Character.getNumericValue(routingNumber.charAt(i)) * weights[i];
            }
            
            return checksum % 10 == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isBankAccountBlacklisted(String accountNumber) {
        // Simulate blacklist check
        return accountNumber != null && accountNumber.contains("0000000");
    }
    
    private boolean isAccountActive(BankTransferDetails bankDetails) {
        // Simulate account status check
        return !bankDetails.getAccountNumber().endsWith("999"); // Mock: accounts ending in 999 are inactive
    }
    
    private PaymentResult simulateBankTransfer(String transactionId, PaymentRequest request, 
                                             BankTransferDetails bankDetails) {
        // Simulate bank processing time
        try {
            Thread.sleep(200 + random.nextInt(300)); // Simulate longer processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate different response scenarios
        double outcome = random.nextDouble();
        
        if (outcome < 0.75) { // 75% success rate
            PaymentResult result = PaymentResult.processing(transactionId);
            result.setMerchantId(request.getMerchantId());
            result.setReferenceId(request.getReferenceId());
            result.setAmount(request.getAmount());
            result.setCurrency(request.getCurrency().getCode());
            result.setPaymentMethodType(request.getPaymentMethodType().getCode());
            result.setProcessorResponse("Bank transfer initiated - processing");
            result.setProcessorTransactionId("bank_" + random.nextInt(1000000));
            return result;
        } else if (outcome < 0.9) { // 15% insufficient funds
            return PaymentResult.failure(transactionId, "INSUFFICIENT_FUNDS", "Insufficient funds in account");
        } else { // 10% other bank errors
            return PaymentResult.failure(transactionId, "BANK_ERROR", "Bank declined the transfer");
        }
    }
}