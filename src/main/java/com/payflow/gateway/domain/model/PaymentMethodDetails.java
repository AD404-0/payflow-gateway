package com.payflow.gateway.domain.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract base class for payment method details
 * Uses Jackson polymorphism for different payment method types
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreditCardDetails.class, name = "credit_card"),
    @JsonSubTypes.Type(value = BankTransferDetails.class, name = "bank_transfer"),
    @JsonSubTypes.Type(value = DigitalWalletDetails.class, name = "digital_wallet")
})
public abstract class PaymentMethodDetails {
    
    /**
     * Returns the payment method type for this details object
     */
    public abstract String getType();
    
    /**
     * Validates the payment method details
     */
    public abstract boolean isValid();
    
    /**
     * Returns masked/safe representation for logging
     */
    public abstract String getMaskedDetails();
}