package com.payflow.gateway.command;

import com.payflow.gateway.domain.model.PaymentResult;
import com.payflow.gateway.processor.PaymentProcessor;

import java.util.Map;

/**
 * Command for voiding/cancelling a payment
 * Can only be used on transactions that haven't been settled
 */
public class VoidCommand extends AbstractPaymentCommand {
    
    private final PaymentProcessor paymentProcessor;
    private final String originalTransactionId;
    private final String reason;
    private PaymentResult voidResult;
    
    public VoidCommand(String transactionId, PaymentProcessor paymentProcessor,
                      String originalTransactionId, String reason, Map<String, Object> parameters) {
        super(CommandType.VOID, transactionId, parameters);
        this.paymentProcessor = paymentProcessor;
        this.originalTransactionId = originalTransactionId;
        this.reason = reason;
    }
    
    @Override
    protected CommandResult doExecute() {
        logger.info("Processing void for original transaction: {} reason: {}", 
                   originalTransactionId, reason);
        
        addExecutionContext("original_transaction_id", originalTransactionId);
        addExecutionContext("reason", reason);
        
        try {
            voidResult = paymentProcessor.voidPayment(originalTransactionId, reason);
            
            addExecutionContext("processor_result", voidResult.isSuccess());
            addExecutionContext("void_transaction_id", voidResult.getTransactionId());
            
            if (voidResult.isSuccess()) {
                return CommandResult.success(getCommandId(), 
                        "Transaction voided successfully", voidResult);
            } else {
                return CommandResult.failure(getCommandId(), 
                        voidResult.getErrorCode(), voidResult.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error voiding transaction: {}", originalTransactionId, e);
            return CommandResult.failure(getCommandId(), "VOID_ERROR", 
                    "Failed to void transaction: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected CommandResult doUndo() {
        // Voiding a void doesn't make sense - would need to re-process the original payment
        return CommandResult.failure(getCommandId(), "VOID_UNDO_NOT_SUPPORTED", 
                "Void operations cannot be undone - original transaction would need to be re-processed");
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
        
        if (reason == null || reason.trim().isEmpty()) {
            return CommandResult.failure(getCommandId(), "NO_REASON", "Void reason is required");
        }
        
        return CommandResult.success(getCommandId(), "Validation passed");
    }
    
    @Override
    protected boolean supportsUndo() {
        return false; // Voids cannot be undone
    }
    
    public PaymentResult getVoidResult() {
        return voidResult;
    }
    
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }
    
    public String getReason() {
        return reason;
    }
}