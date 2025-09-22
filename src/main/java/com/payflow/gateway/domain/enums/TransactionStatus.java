package com.payflow.gateway.domain.enums;

/**
 * Enumeration for transaction states in the PayFlow system
 * Implements state machine for payment lifecycle management
 */
public enum TransactionStatus {
    PENDING("pending", "Transaction initiated but not yet processed"),
    PROCESSING("processing", "Transaction is being processed"),
    COMPLETED("completed", "Transaction completed successfully"),
    FAILED("failed", "Transaction failed"),
    CANCELLED("cancelled", "Transaction was cancelled"),
    REFUNDED("refunded", "Transaction was refunded"),
    PARTIALLY_REFUNDED("partially_refunded", "Transaction was partially refunded"),
    DISPUTED("disputed", "Transaction is under dispute"),
    SETTLED("settled", "Transaction has been settled");
    
    private final String code;
    private final String description;
    
    TransactionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if transition from current status to target status is valid
     */
    public boolean canTransitionTo(TransactionStatus targetStatus) {
        return switch (this) {
            case PENDING -> targetStatus == PROCESSING || targetStatus == CANCELLED || targetStatus == FAILED;
            case PROCESSING -> targetStatus == COMPLETED || targetStatus == FAILED || targetStatus == CANCELLED;
            case COMPLETED -> targetStatus == REFUNDED || targetStatus == PARTIALLY_REFUNDED || 
                             targetStatus == DISPUTED || targetStatus == SETTLED;
            case FAILED, CANCELLED -> false; // Terminal states
            case REFUNDED, PARTIALLY_REFUNDED -> targetStatus == DISPUTED;
            case DISPUTED -> targetStatus == COMPLETED || targetStatus == REFUNDED;
            case SETTLED -> false; // Terminal state
        };
    }
    
    public boolean isTerminal() {
        return this == FAILED || this == CANCELLED || this == SETTLED;
    }
}