package com.payflow.gateway.domain.model;

import jakarta.validation.constraints.*;

/**
 * Digital wallet payment method details (PayPal, Apple Pay, Google Pay, etc.)
 */
public class DigitalWalletDetails extends PaymentMethodDetails {
    
    @NotBlank(message = "Wallet provider is required")
    private String walletProvider; // PAYPAL, APPLE_PAY, GOOGLE_PAY, etc.
    
    @NotBlank(message = "Wallet ID is required")
    private String walletId;
    
    @Email(message = "Valid email address is required")
    private String walletEmail;
    
    private String walletToken;
    
    private String deviceId;
    
    private String fingerprintId;
    
    // Constructors
    public DigitalWalletDetails() {}
    
    public DigitalWalletDetails(String walletProvider, String walletId) {
        this.walletProvider = walletProvider;
        this.walletId = walletId;
    }
    
    @Override
    public String getType() {
        return "digital_wallet";
    }
    
    @Override
    public boolean isValid() {
        return walletProvider != null && !walletProvider.trim().isEmpty() &&
               walletId != null && !walletId.trim().isEmpty();
    }
    
    @Override
    public String getMaskedDetails() {
        if (walletEmail != null) {
            int atIndex = walletEmail.indexOf('@');
            if (atIndex > 0) {
                String localPart = walletEmail.substring(0, atIndex);
                String domain = walletEmail.substring(atIndex);
                String maskedLocal = localPart.length() > 2 ? 
                    localPart.substring(0, 2) + "****" : "****";
                return maskedLocal + domain;
            }
        }
        
        if (walletId != null && walletId.length() > 4) {
            return walletProvider + " ****" + walletId.substring(walletId.length() - 4);
        }
        
        return walletProvider + " ****";
    }
    
    // Getters and Setters
    public String getWalletProvider() {
        return walletProvider;
    }
    
    public void setWalletProvider(String walletProvider) {
        this.walletProvider = walletProvider;
    }
    
    public String getWalletId() {
        return walletId;
    }
    
    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }
    
    public String getWalletEmail() {
        return walletEmail;
    }
    
    public void setWalletEmail(String walletEmail) {
        this.walletEmail = walletEmail;
    }
    
    public String getWalletToken() {
        return walletToken;
    }
    
    public void setWalletToken(String walletToken) {
        this.walletToken = walletToken;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getFingerprintId() {
        return fingerprintId;
    }
    
    public void setFingerprintId(String fingerprintId) {
        this.fingerprintId = fingerprintId;
    }
}