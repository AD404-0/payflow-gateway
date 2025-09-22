package com.payflow.gateway.domain.model;

import jakarta.validation.constraints.*;

/**
 * Bank transfer payment method details
 */
public class BankTransferDetails extends PaymentMethodDetails {
    
    @NotBlank(message = "Account number is required")
    @Size(max = 34, message = "Account number cannot exceed 34 characters")
    private String accountNumber;
    
    @NotBlank(message = "Routing number is required")
    @Pattern(regexp = "^[0-9]{9}$", message = "Routing number must be 9 digits")
    private String routingNumber;
    
    @NotBlank(message = "Account holder name is required")
    @Size(max = 100, message = "Account holder name cannot exceed 100 characters")
    private String accountHolderName;
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;
    
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$", 
            message = "Invalid IBAN format")
    private String iban;
    
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", 
            message = "Invalid SWIFT code format")
    private String swiftCode;
    
    private String accountType; // CHECKING, SAVINGS
    
    // Constructors
    public BankTransferDetails() {}
    
    public BankTransferDetails(String accountNumber, String routingNumber, 
                              String accountHolderName, String bankName) {
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
        this.accountHolderName = accountHolderName;
        this.bankName = bankName;
    }
    
    @Override
    public String getType() {
        return "bank_transfer";
    }
    
    @Override
    public boolean isValid() {
        return accountNumber != null && !accountNumber.trim().isEmpty() &&
               routingNumber != null && routingNumber.matches("^[0-9]{9}$") &&
               accountHolderName != null && !accountHolderName.trim().isEmpty() &&
               bankName != null && !bankName.trim().isEmpty();
    }
    
    @Override
    public String getMaskedDetails() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
    
    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getRoutingNumber() {
        return routingNumber;
    }
    
    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }
    
    public String getAccountHolderName() {
        return accountHolderName;
    }
    
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getIban() {
        return iban;
    }
    
    public void setIban(String iban) {
        this.iban = iban;
    }
    
    public String getSwiftCode() {
        return swiftCode;
    }
    
    public void setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}