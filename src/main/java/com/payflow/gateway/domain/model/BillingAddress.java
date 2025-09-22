package com.payflow.gateway.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents billing address information for payment processing
 */
public class BillingAddress {
    
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 100, message = "Address line 1 cannot exceed 100 characters")
    private String addressLine1;
    
    @Size(max = 100, message = "Address line 2 cannot exceed 100 characters")
    private String addressLine2;
    
    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City cannot exceed 50 characters")
    private String city;
    
    @Size(max = 20, message = "State cannot exceed 20 characters")
    private String state;
    
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;
    
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 3, message = "Country code must be 2 or 3 characters")
    private String country;
    
    // Constructors
    public BillingAddress() {}
    
    public BillingAddress(String addressLine1, String city, String country) {
        this.addressLine1 = addressLine1;
        this.city = city;
        this.country = country;
    }
    
    // Getters and Setters
    public String getAddressLine1() {
        return addressLine1;
    }
    
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }
    
    public String getAddressLine2() {
        return addressLine2;
    }
    
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    @Override
    public String toString() {
        return String.format("%s, %s, %s %s, %s",
                addressLine1, city, state != null ? state : "", postalCode != null ? postalCode : "", country);
    }
}