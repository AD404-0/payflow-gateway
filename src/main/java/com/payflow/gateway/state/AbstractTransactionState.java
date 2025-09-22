package com.payflow.gateway.state;

import com.payflow.gateway.domain.enums.TransactionStatus;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract base class for transaction states
 * Provides default implementations and common behavior
 */
public abstract class AbstractTransactionState implements TransactionState {
    
    @Override
    public StateTransitionResult process(TransactionContext context, String reason) {
        if (canTransitionTo(TransactionStatus.PROCESSING)) {
            context.setState(new ProcessingState(), reason);
            return StateTransitionResult.success(getStatus(), TransactionStatus.PROCESSING, 
                                                "Transaction moved to processing", context.getTransactionId());
        }
        return invalidTransition(TransactionStatus.PROCESSING, context);
    }
    
    @Override
    public StateTransitionResult complete(TransactionContext context, String authorizationCode) {
        if (canTransitionTo(TransactionStatus.COMPLETED)) {
            context.setState(new CompletedState(), "Transaction completed with auth code: " + authorizationCode);
            return StateTransitionResult.success(getStatus(), TransactionStatus.COMPLETED, 
                                                "Transaction completed successfully", authorizationCode, context.getTransactionId());
        }
        return invalidTransition(TransactionStatus.COMPLETED, context);
    }
    
    @Override
    public StateTransitionResult fail(TransactionContext context, String errorCode, String errorMessage) {
        if (canTransitionTo(TransactionStatus.FAILED)) {
            context.setState(new FailedState(), "Transaction failed: " + errorMessage);
            return StateTransitionResult.success(getStatus(), TransactionStatus.FAILED, 
                                                "Transaction failed: " + errorMessage, context.getTransactionId());
        }
        return invalidTransition(TransactionStatus.FAILED, context);
    }
    
    @Override
    public StateTransitionResult cancel(TransactionContext context, String reason) {
        if (canTransitionTo(TransactionStatus.CANCELLED)) {
            context.setState(new CancelledState(), reason);
            return StateTransitionResult.success(getStatus(), TransactionStatus.CANCELLED, 
                                                "Transaction cancelled: " + reason, context.getTransactionId());
        }
        return invalidTransition(TransactionStatus.CANCELLED, context);
    }
    
    @Override
    public StateTransitionResult refund(TransactionContext context, String amount, String reason) {
        if (canTransitionTo(TransactionStatus.REFUNDED)) {
            context.setState(new RefundedState(), "Full refund: " + reason);
            return StateTransitionResult.success(getStatus(), TransactionStatus.REFUNDED, 
                                                "Transaction refunded: " + amount, context.getTransactionId());
        }
        return invalidTransition(TransactionStatus.REFUNDED, context);
    }
    
    @Override
    public StateTransitionResult partialRefund(TransactionContext context, String amount, String reason) {
        if (canTransitionTo(TransactionStatus.PARTIALLY_REFUNDED)) {
            context.setState(new PartiallyRefundedState(), "Partial refund: " + reason);
            return StateTransitionResult.success(getStatus(), TransactionStatus.PARTIALLY_REFUNDED, 
                                                "Transaction partially refunded: " + amount, context.getTransactionId());
        }
        return invalidTransition(TransactionStatus.PARTIALLY_REFUNDED, context);
    }
    
    @Override
    public StateTransitionResult dispute(TransactionContext context, String reason) {
        if (canTransitionTo(TransactionStatus.DISPUTED)) {
            context.setState(new DisputedState(), "Transaction disputed: " + reason);
            return StateTransitionResult.success(getStatus(), TransactionStatus.DISPUTED, 
                                                "Transaction disputed: " + reason, context.getTransactionId());
        }
        return invalidTransition(TransactionStatus.DISPUTED, context);
    }
    
    @Override
    public StateTransitionResult settle(TransactionContext context) {
        if (canTransitionTo(TransactionStatus.SETTLED)) {
            context.setState(new SettledState(), "Transaction settled");
            return StateTransitionResult.success(getStatus(), TransactionStatus.SETTLED, 
                                                "Transaction settled", context.getTransactionId());
        }
        return invalidTransition(TransactionStatus.SETTLED, context);
    }
    
    @Override
    public boolean canTransitionTo(TransactionStatus targetStatus) {
        return getStatus().canTransitionTo(targetStatus);
    }
    
    @Override
    public List<TransactionStatus> getAvailableTransitions() {
        return Arrays.stream(TransactionStatus.values())
                .filter(this::canTransitionTo)
                .toList();
    }
    
    @Override
    public boolean isTerminal() {
        return getStatus().isTerminal();
    }
    
    protected StateTransitionResult invalidTransition(TransactionStatus targetStatus, TransactionContext context) {
        return StateTransitionResult.invalidTransition(getStatus(), targetStatus, context.getTransactionId());
    }
}