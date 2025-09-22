package com.payflow.gateway.domain.enums;

/**
 * Currency codes following ISO 4217 standard
 */
public enum Currency {
    USD("USD", "US Dollar", 2),
    EUR("EUR", "Euro", 2),
    GBP("GBP", "British Pound", 2),
    JPY("JPY", "Japanese Yen", 0),
    CAD("CAD", "Canadian Dollar", 2),
    AUD("AUD", "Australian Dollar", 2),
    CHF("CHF", "Swiss Franc", 2),
    CNY("CNY", "Chinese Yuan", 2),
    INR("INR", "Indian Rupee", 2),
    BTC("BTC", "Bitcoin", 8);
    
    private final String code;
    private final String displayName;
    private final int decimalPlaces;
    
    Currency(String code, String displayName, int decimalPlaces) {
        this.code = code;
        this.displayName = displayName;
        this.decimalPlaces = decimalPlaces;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getDecimalPlaces() {
        return decimalPlaces;
    }
    
    public static Currency fromCode(String code) {
        for (Currency currency : values()) {
            if (currency.code.equalsIgnoreCase(code)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Unknown currency code: " + code);
    }
}