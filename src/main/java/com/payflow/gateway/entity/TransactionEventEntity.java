package com.payflow.gateway.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "transaction_events")
public class TransactionEventEntity {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public TransactionEntity getTransaction() { return transaction; }
    public void setTransaction(TransactionEntity transaction) { this.transaction = transaction; }
}