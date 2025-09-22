package com.payflow.gateway.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payment_methods")
public class PaymentMethodEntity {
    @Id
    private String id;
    
    @OneToOne
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public TransactionEntity getTransaction() { return transaction; }
    public void setTransaction(TransactionEntity transaction) { this.transaction = transaction; }
}