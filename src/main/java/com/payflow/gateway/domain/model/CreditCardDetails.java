package com.payflow.gateway.domain.model;

import jakarta.validation.constraints.*;
import java.time.YearMonth;

/**
 * Credit card payment method details
 */
public class CreditCardDetails extends PaymentMethodDetails {
    
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be 13-19 digits")
    private String cardNumber;
    
    @NotBlank(message = "Cardholder name is required")
    @Size(max = 100, message = "Cardholder name cannot exceed 100 characters")
    private String cardholderName;
    
    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    private Integer expiryMonth;
    
    @NotNull(message = "Expiry year is required")
    @Min(value = 2024, message = "Expiry year must be current year or later")
    private Integer expiryYear;
    
    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;
    
    private String issuer; // VISA, MASTERCARD, etc.
    
    // Constructors
    public CreditCardDetails() {}
    
    public CreditCardDetails(String cardNumber, String cardholderName, 
                           Integer expiryMonth, Integer expiryYear, String cvv) {
        this.cardNumber = cardNumber;
        this.cardholderName = cardholderName;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
    }
    
    @Override
    public String getType() {
        return "credit_card";
    }
    
    @Override
    public boolean isValid() {
        if (cardNumber == null || cardholderName == null || 
            expiryMonth == null || expiryYear == null || cvv == null) {
            return false;
        }
        
        // Check if card is not expired
        YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
        YearMonth current = YearMonth.now();
        
        return !expiry.isBefore(current) && isValidCardNumber(cardNumber);
    }
    
    @Override
    public String getMaskedDetails() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
    
    /**
     * Basic Luhn algorithm validation for card number
     */
    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }
    
    // Getters and Setters
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getCardholderName() {
        return cardholderName;
    }
    
    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }
    
    public Integer getExpiryMonth() {
        return expiryMonth;
    }
    
    public void setExpiryMonth(Integer expiryMonth) {
        this.expiryMonth = expiryMonth;
    }
    
    public Integer getExpiryYear() {
        return expiryYear;
    }
    
    public void setExpiryYear(Integer expiryYear) {
        this.expiryYear = expiryYear;
    }
    
    public String getCvv() {
        return cvv;
    }
    
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}