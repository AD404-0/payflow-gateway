package com.payflow.gateway.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "settlements")
public class SettlementEntity {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private MerchantEntity merchant;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public MerchantEntity getMerchant() { return merchant; }
    public void setMerchant(MerchantEntity merchant) { this.merchant = merchant; }
}