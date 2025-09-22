package com.payflow.gateway.state;

import com.payflow.gateway.domain.enums.TransactionStatus;

/**
 * Context class for transaction state management
 * Implements the State pattern for transaction lifecycle
 */
public class TransactionContext {
    
    private TransactionState currentState;
    private final String transactionId;
    private String lastStateChangeReason;
    private java.time.LocalDateTime lastStateChangeTime;
    
    public TransactionContext(String transactionId) {
        this.transactionId = transactionId;
        this.currentState = new PendingState();
        this.lastStateChangeTime = java.time.LocalDateTime.now();
    }
    
    public TransactionContext(String transactionId, TransactionStatus initialStatus) {
        this.transactionId = transactionId;
        this.currentState = createStateFromStatus(initialStatus);
        this.lastStateChangeTime = java.time.LocalDateTime.now();
    }
    
    /**
     * Processes the transaction to the next state
     */
    public StateTransitionResult process(String reason) {
        return currentState.process(this, reason);
    }
    
    /**
     * Completes the transaction
     */
    public StateTransitionResult complete(String authorizationCode) {
        return currentState.complete(this, authorizationCode);
    }
    
    /**
     * Fails the transaction
     */
    public StateTransitionResult fail(String errorCode, String errorMessage) {
        return currentState.fail(this, errorCode, errorMessage);
    }
    
    /**
     * Cancels the transaction
     */
    public StateTransitionResult cancel(String reason) {
        return currentState.cancel(this, reason);
    }
    
    /**
     * Refunds the transaction
     */
    public StateTransitionResult refund(String amount, String reason) {
        return currentState.refund(this, amount, reason);
    }
    
    /**
     * Partially refunds the transaction
     */
    public StateTransitionResult partialRefund(String amount, String reason) {
        return currentState.partialRefund(this, amount, reason);
    }
    
    /**
     * Disputes the transaction
     */
    public StateTransitionResult dispute(String reason) {
        return currentState.dispute(this, reason);
    }
    
    /**
     * Settles the transaction
     */
    public StateTransitionResult settle() {
        return currentState.settle(this);
    }
    
    /**
     * Changes the state of the transaction
     */
    public void setState(TransactionState newState, String reason) {
        this.currentState = newState;
        this.lastStateChangeReason = reason;
        this.lastStateChangeTime = java.time.LocalDateTime.now();
    }
    
    /**
     * Gets the current transaction status
     */
    public TransactionStatus getStatus() {
        return currentState.getStatus();
    }
    
    /**
     * Checks if the transaction can transition to a specific status
     */
    public boolean canTransitionTo(TransactionStatus targetStatus) {
        return currentState.canTransitionTo(targetStatus);
    }
    
    /**
     * Gets available transition options from current state
     */
    public java.util.List<TransactionStatus> getAvailableTransitions() {
        return currentState.getAvailableTransitions();
    }
    
    /**
     * Checks if the current state is terminal
     */
    public boolean isTerminalState() {
        return currentState.isTerminal();
    }
    
    // Getters
    public String getTransactionId() {
        return transactionId;
    }
    
    public TransactionState getCurrentState() {
        return currentState;
    }
    
    public String getLastStateChangeReason() {
        return lastStateChangeReason;
    }
    
    public java.time.LocalDateTime getLastStateChangeTime() {
        return lastStateChangeTime;
    }
    
    /**
     * Factory method to create state objects from status enum
     */
    private TransactionState createStateFromStatus(TransactionStatus status) {
        return switch (status) {
            case PENDING -> new PendingState();
            case PROCESSING -> new ProcessingState();
            case COMPLETED -> new CompletedState();
            case FAILED -> new FailedState();
            case CANCELLED -> new CancelledState();
            case REFUNDED -> new RefundedState();
            case PARTIALLY_REFUNDED -> new PartiallyRefundedState();
            case DISPUTED -> new DisputedState();
            case SETTLED -> new SettledState();
        };
    }
}