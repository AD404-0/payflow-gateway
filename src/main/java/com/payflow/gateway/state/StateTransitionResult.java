package com.payflow.gateway.state;

import com.payflow.gateway.domain.enums.TransactionStatus;
import java.time.LocalDateTime;

/**
 * Result of a state transition operation
 */
public class StateTransitionResult {
    
    private final boolean success;
    private final TransactionStatus fromStatus;
    private final TransactionStatus toStatus;
    private final String message;
    private final String errorCode;
    private final LocalDateTime timestamp;
    private final String authorizationCode;
    private final String transactionId;
    
    // Private constructor - use factory methods
    private StateTransitionResult(boolean success, TransactionStatus fromStatus, 
                                TransactionStatus toStatus, String message, String errorCode,
                                String authorizationCode, String transactionId) {
        this.success = success;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.message = message;
        this.errorCode = errorCode;
        this.authorizationCode = authorizationCode;
        this.transactionId = transactionId;
        this.timestamp = LocalDateTime.now();
    }
    
    // Factory methods for success cases
    public static StateTransitionResult success(TransactionStatus fromStatus, TransactionStatus toStatus, 
                                              String message, String transactionId) {
        return new StateTransitionResult(true, fromStatus, toStatus, message, null, null, transactionId);
    }
    
    public static StateTransitionResult success(TransactionStatus fromStatus, TransactionStatus toStatus, 
                                              String message, String authorizationCode, String transactionId) {
        return new StateTransitionResult(true, fromStatus, toStatus, message, null, authorizationCode, transactionId);
    }
    
    // Factory methods for failure cases
    public static StateTransitionResult failure(TransactionStatus currentStatus, String errorCode, 
                                               String message, String transactionId) {
        return new StateTransitionResult(false, currentStatus, currentStatus, message, errorCode, null, transactionId);
    }
    
    public static StateTransitionResult invalidTransition(TransactionStatus fromStatus, 
                                                        TransactionStatus attemptedToStatus, String transactionId) {
        String message = String.format("Invalid transition from %s to %s", fromStatus, attemptedToStatus);
        return new StateTransitionResult(false, fromStatus, fromStatus, message, "INVALID_TRANSITION", null, transactionId);
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public TransactionStatus getFromStatus() {
        return fromStatus;
    }
    
    public TransactionStatus getToStatus() {
        return toStatus;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getAuthorizationCode() {
        return authorizationCode;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public boolean hasAuthorizationCode() {
        return authorizationCode != null && !authorizationCode.trim().isEmpty();
    }
    
    public boolean isStateChanged() {
        return fromStatus != toStatus;
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("StateTransition[SUCCESS: %s -> %s, %s]", 
                               fromStatus, toStatus, message);
        } else {
            return String.format("StateTransition[FAILURE: %s, Error: %s, %s]", 
                               fromStatus, errorCode, message);
        }
    }
}