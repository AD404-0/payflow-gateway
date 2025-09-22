package com.payflow.gateway.state;

import com.payflow.gateway.domain.enums.TransactionStatus;
import java.util.List;

/**
 * Interface defining the state behavior for transaction lifecycle
 * Part of the State pattern implementation
 */
public interface TransactionState {
    
    /**
     * Gets the transaction status represented by this state
     */
    TransactionStatus getStatus();
    
    /**
     * Processes the transaction to the next appropriate state
     */
    StateTransitionResult process(TransactionContext context, String reason);
    
    /**
     * Completes the transaction
     */
    StateTransitionResult complete(TransactionContext context, String authorizationCode);
    
    /**
     * Fails the transaction
     */
    StateTransitionResult fail(TransactionContext context, String errorCode, String errorMessage);
    
    /**
     * Cancels the transaction
     */
    StateTransitionResult cancel(TransactionContext context, String reason);
    
    /**
     * Refunds the transaction
     */
    StateTransitionResult refund(TransactionContext context, String amount, String reason);
    
    /**
     * Partially refunds the transaction
     */
    StateTransitionResult partialRefund(TransactionContext context, String amount, String reason);
    
    /**
     * Disputes the transaction
     */
    StateTransitionResult dispute(TransactionContext context, String reason);
    
    /**
     * Settles the transaction
     */
    StateTransitionResult settle(TransactionContext context);
    
    /**
     * Checks if transition to target status is allowed
     */
    boolean canTransitionTo(TransactionStatus targetStatus);
    
    /**
     * Gets list of available transitions from this state
     */
    List<TransactionStatus> getAvailableTransitions();
    
    /**
     * Checks if this is a terminal state
     */
    boolean isTerminal();
    
    /**
     * Gets a human-readable description of this state
     */
    String getDescription();
}