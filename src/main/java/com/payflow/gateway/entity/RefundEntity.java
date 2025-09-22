package com.payflow.gateway.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "refunds")
public class RefundEntity {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "original_transaction_id")
    private TransactionEntity originalTransaction;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public TransactionEntity getOriginalTransaction() { return originalTransaction; }
    public void setOriginalTransaction(TransactionEntity originalTransaction) { this.originalTransaction = originalTransaction; }
}