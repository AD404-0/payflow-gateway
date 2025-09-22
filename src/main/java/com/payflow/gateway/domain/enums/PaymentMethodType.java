package com.payflow.gateway.domain.enums;

/**
 * Enumeration for different payment method types supported by PayFlow
 */
public enum PaymentMethodType {
    CREDIT_CARD("credit_card", "Credit Card"),
    DEBIT_CARD("debit_card", "Debit Card"), 
    BANK_TRANSFER("bank_transfer", "Bank Transfer"),
    DIGITAL_WALLET("digital_wallet", "Digital Wallet"),
    CRYPTOCURRENCY("cryptocurrency", "Cryptocurrency");
    
    private final String code;
    private final String displayName;
    
    PaymentMethodType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static PaymentMethodType fromCode(String code) {
        for (PaymentMethodType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown payment method type: " + code);
    }
}