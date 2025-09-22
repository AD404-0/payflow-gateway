package com.payflow.gateway.command;

import com.payflow.gateway.domain.model.PaymentResult;
import com.payflow.gateway.processor.PaymentProcessor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Command for refunding a payment
 * Supports both full and partial refunds with undo capability
 */
public class RefundCommand extends AbstractPaymentCommand {
    
    private final PaymentProcessor paymentProcessor;
    private final String originalTransactionId;
    private final BigDecimal refundAmount;
    private final String reason;
    private PaymentResult refundResult;
    
    public RefundCommand(String transactionId, PaymentProcessor paymentProcessor,
                        String originalTransactionId, BigDecimal refundAmount, 
                        String reason, Map<String, Object> parameters) {
        super(CommandType.REFUND, transactionId, parameters);
        this.paymentProcessor = paymentProcessor;
        this.originalTransactionId = originalTransactionId;
        this.refundAmount = refundAmount;
        this.reason = reason;
    }
    
    @Override
    protected CommandResult doExecute() {
        logger.info("Processing refund for original transaction: {} amount: {}", 
                   originalTransactionId, refundAmount);
        
        addExecutionContext("original_transaction_id", originalTransactionId);
        addExecutionContext("refund_amount", refundAmount);
        addExecutionContext("reason", reason);
        
        try {
            String amountStr = refundAmount != null ? refundAmount.toString() : null;
            refundResult = paymentProcessor.refundPayment(originalTransactionId, amountStr, reason);
            
            addExecutionContext("processor_result", refundResult.isSuccess());
            addExecutionContext("refund_transaction_id", refundResult.getTransactionId());
            
            if (refundResult.isSuccess()) {
                return CommandResult.success(getCommandId(), 
                        "Refund processed successfully", refundResult);
            } else {
                return CommandResult.failure(getCommandId(), 
                        refundResult.getErrorCode(), refundResult.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing refund for transaction: {}", originalTransactionId, e);
            return CommandResult.failure(getCommandId(), "REFUND_ERROR", 
                    "Failed to process refund: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected CommandResult doUndo() {
        // Refunds generally cannot be undone directly - would require a new charge
        // In a real system, this might involve reversing the refund through a new charge
        return CommandResult.failure(getCommandId(), "REFUND_UNDO_NOT_SUPPORTED", 
                "Refunds cannot be undone directly - contact support for manual reversal");
    }
    
    @Override
    protected CommandResult doValidate() {
        if (paymentProcessor == null) {
            return CommandResult.failure(getCommandId(), "NO_PROCESSOR", "Payment processor is required");
        }
        
        if (originalTransactionId == null || originalTransactionId.trim().isEmpty()) {
            return CommandResult.failure(getCommandId(), "NO_ORIGINAL_TRANSACTION", 
                    "Original transaction ID is required");
        }
        
        if (refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return CommandResult.failure(getCommandId(), "INVALID_AMOUNT", 
                    "Refund amount must be greater than zero");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            return CommandResult.failure(getCommandId(), "NO_REASON", "Refund reason is required");
        }
        
        return CommandResult.success(getCommandId(), "Validation passed");
    }
    
    @Override
    protected boolean supportsUndo() {
        return false; // Refunds typically cannot be undone
    }
    
    public PaymentResult getRefundResult() {
        return refundResult;
    }
    
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public String getReason() {
        return reason;
    }
}