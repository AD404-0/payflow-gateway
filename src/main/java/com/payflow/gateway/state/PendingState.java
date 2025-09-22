package com.payflow.gateway.state;

import com.payflow.gateway.domain.enums.TransactionStatus;

/**
 * Pending state - initial state when transaction is created
 */
public class PendingState extends AbstractTransactionState {
    
    @Override
    public TransactionStatus getStatus() {
        return TransactionStatus.PENDING;
    }
    
    @Override
    public String getDescription() {
        return "Transaction has been created and is pending processing";
    }
}

/**
 * Processing state - transaction is being processed
 */
class ProcessingState extends AbstractTransactionState {
    
    @Override
    public TransactionStatus getStatus() {
        return TransactionStatus.PROCESSING;
    }
    
    @Override
    public String getDescription() {
        return "Transaction is currently being processed";
    }
}

/**
 * Completed state - transaction completed successfully
 */
class CompletedState extends AbstractTransactionState {
    
    @Override
    public TransactionStatus getStatus() {
        return TransactionStatus.COMPLETED;
    }
    
    @Override
    public String getDescription() {
        return "Transaction has been completed successfully";
    }
}

/**
 * Failed state - transaction failed (terminal state)
 */
class FailedState extends AbstractTransactionState {
    
    @Override
    public TransactionStatus getStatus() {
        return TransactionStatus.FAILED;
    }
    
    @Override
    public String getDescription() {
        return "Transaction has failed and cannot be processed";
    }
}

/**
 * Cancelled state - transaction was cancelled (terminal state)
 */
class CancelledState extends AbstractTransactionState {
    
    @Override
    public TransactionStatus getStatus() {
        return TransactionStatus.CANCELLED;
    }
    
    @Override
    public String getDescription() {
        return "Transaction has been cancelled";
    }
}

/**
 * Refunded state - transaction was fully refunded
 */
class RefundedState extends AbstractTransactionState {
    
    @Override
    public TransactionStatus getStatus() {
        return TransactionStatus.REFUNDED;
    }
    
    @Override
    public String getDescription() {
        return "Transaction has been fully refunded";
    }
}

/**
 * Partially Refunded state - transaction was partially refunded
 */
class PartiallyRefundedState extends AbstractTransactionState {
    
    @Override
    public TransactionStatus getStatus() {
        return TransactionStatus.PARTIALLY_REFUNDED;
    }
    
    @Override
    public String getDescription() {
        return "Transaction has been partially refunded";
    }
}

/**
 * Disputed state - transaction is under dispute
 */
class DisputedState extends AbstractTransactionState {
    
    @Override
    public TransactionStatus getStatus() {
        return TransactionStatus.DISPUTED;
    }
    
    @Override
    public String getDescription() {
        return "Transaction is under dispute/chargeback";
    }
}

/**
 * Settled state - transaction has been settled (terminal state)
 */
class SettledState extends AbstractTransactionState {
    
    @Override
    public TransactionStatus getStatus() {
        return TransactionStatus.SETTLED;
    }
    
    @Override
    public String getDescription() {
        return "Transaction has been settled with the merchant";
    }
}