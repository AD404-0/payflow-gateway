package com.payflow.gateway.command;

import com.payflow.gateway.domain.model.PaymentRequest;
import com.payflow.gateway.domain.model.PaymentResult;
import com.payflow.gateway.processor.PaymentProcessor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Command for charging a payment
 * Implements the Command pattern for payment processing operations
 */
public class ChargeCommand extends AbstractPaymentCommand {
    
    private final PaymentProcessor paymentProcessor;
    private final PaymentRequest paymentRequest;
    private PaymentResult paymentResult;
    
    public ChargeCommand(String transactionId, PaymentProcessor paymentProcessor, 
                        PaymentRequest paymentRequest, Map<String, Object> parameters) {
        super(CommandType.CHARGE, transactionId, parameters);
        this.paymentProcessor = paymentProcessor;
        this.paymentRequest = paymentRequest;
    }
    
    @Override
    protected CommandResult doExecute() {
        logger.info("Processing charge for transaction: {} amount: {}", 
                   getTransactionId(), paymentRequest.getAmount());
        
        addExecutionContext("merchant_id", paymentRequest.getMerchantId());
        addExecutionContext("amount", paymentRequest.getAmount());
        addExecutionContext("currency", paymentRequest.getCurrency());
        addExecutionContext("payment_method", paymentRequest.getPaymentMethodType());
        
        try {
            paymentResult = paymentProcessor.processPayment(paymentRequest);
            
            addExecutionContext("processor_result", paymentResult.isSuccess());
            addExecutionContext("authorization_code", paymentResult.getAuthorizationCode());
            
            if (paymentResult.isSuccess()) {
                return CommandResult.success(getCommandId(), 
                        "Payment charged successfully", paymentResult);
            } else {
                return CommandResult.failure(getCommandId(), 
                        paymentResult.getErrorCode(), paymentResult.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing charge for transaction: {}", getTransactionId(), e);
            return CommandResult.failure(getCommandId(), "CHARGE_ERROR", 
                    "Failed to process charge: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected CommandResult doUndo() {
        if (paymentResult == null || !paymentResult.isSuccess()) {
            return CommandResult.failure(getCommandId(), "NO_CHARGE_TO_UNDO", 
                    "No successful charge to undo");
        }
        
        logger.info("Voiding charge for transaction: {}", getTransactionId());
        
        try {
            PaymentResult voidResult = paymentProcessor.voidPayment(
                    paymentResult.getTransactionId(), "Undo charge command");
            
            addExecutionContext("void_result", voidResult.isSuccess());
            
            if (voidResult.isSuccess()) {
                return CommandResult.success(getCommandId(), 
                        "Charge voided successfully", voidResult);
            } else {
                return CommandResult.failure(getCommandId(), 
                        voidResult.getErrorCode(), voidResult.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error voiding charge for transaction: {}", getTransactionId(), e);
            return CommandResult.failure(getCommandId(), "VOID_ERROR", 
                    "Failed to void charge: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected CommandResult doValidate() {
        if (paymentProcessor == null) {
            return CommandResult.failure(getCommandId(), "NO_PROCESSOR", "Payment processor is required");
        }
        
        if (paymentRequest == null) {
            return CommandResult.failure(getCommandId(), "NO_REQUEST", "Payment request is required");
        }
        
        if (!paymentProcessor.canProcess(paymentRequest)) {
            return CommandResult.failure(getCommandId(), "PROCESSOR_MISMATCH", 
                    "Processor cannot handle this payment method");
        }
        
        return CommandResult.success(getCommandId(), "Validation passed");
    }
    
    @Override
    protected boolean supportsUndo() {
        return true; // Charges can be voided
    }
    
    public PaymentResult getPaymentResult() {
        return paymentResult;
    }
}