package com.payflow.gateway.domain.model;

import com.payflow.gateway.domain.enums.Currency;
import com.payflow.gateway.domain.enums.PaymentMethodType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Request Domain Model Tests")
public class PaymentRequestTest {

    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
    }

    @Test
    @DisplayName("Should create payment request with valid data")
    void testValidPaymentRequest() {
        // Arrange & Act
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrency(Currency.USD);
        paymentRequest.setPaymentMethodType(PaymentMethodType.CREDIT_CARD);
        paymentRequest.setMerchantId("test-merchant");
        paymentRequest.setDescription("Test payment");

        // Assert
        assertEquals(new BigDecimal("100.00"), paymentRequest.getAmount());
        assertEquals(Currency.USD, paymentRequest.getCurrency());
        assertEquals(PaymentMethodType.CREDIT_CARD, paymentRequest.getPaymentMethodType());
        assertEquals("test-merchant", paymentRequest.getMerchantId());
        assertEquals("Test payment", paymentRequest.getDescription());
    }

    @Test
    @DisplayName("Should handle different currencies")
    void testDifferentCurrencies() {
        // Test USD
        paymentRequest.setCurrency(Currency.USD);
        assertEquals(Currency.USD, paymentRequest.getCurrency());

        // Test EUR
        paymentRequest.setCurrency(Currency.EUR);
        assertEquals(Currency.EUR, paymentRequest.getCurrency());

        // Test GBP
        paymentRequest.setCurrency(Currency.GBP);
        assertEquals(Currency.GBP, paymentRequest.getCurrency());
    }

    @Test
    @DisplayName("Should handle different payment method types")
    void testDifferentPaymentMethodTypes() {
        // Test Credit Card
        paymentRequest.setPaymentMethodType(PaymentMethodType.CREDIT_CARD);
        assertEquals(PaymentMethodType.CREDIT_CARD, paymentRequest.getPaymentMethodType());

        // Test Bank Transfer
        paymentRequest.setPaymentMethodType(PaymentMethodType.BANK_TRANSFER);
        assertEquals(PaymentMethodType.BANK_TRANSFER, paymentRequest.getPaymentMethodType());

        // Test Digital Wallet
        paymentRequest.setPaymentMethodType(PaymentMethodType.DIGITAL_WALLET);
        assertEquals(PaymentMethodType.DIGITAL_WALLET, paymentRequest.getPaymentMethodType());
    }

    @Test
    @DisplayName("Should handle optional fields")
    void testOptionalFields() {
        // Test with minimal required fields
        paymentRequest.setAmount(new BigDecimal("50.00"));
        paymentRequest.setCurrency(Currency.USD);
        paymentRequest.setPaymentMethodType(PaymentMethodType.CREDIT_CARD);
        paymentRequest.setMerchantId("test-merchant");

        assertNotNull(paymentRequest.getAmount());
        assertNotNull(paymentRequest.getCurrency());
        assertNotNull(paymentRequest.getPaymentMethodType());
        assertNotNull(paymentRequest.getMerchantId());

        // Optional fields should be null
        assertNull(paymentRequest.getCustomerEmail());
        assertNull(paymentRequest.getCustomerPhone());
        assertNull(paymentRequest.getCallbackUrl());
    }

    @Test
    @DisplayName("Should accept customer information")
    void testCustomerInformation() {
        // Act
        paymentRequest.setCustomerEmail("customer@example.com");
        paymentRequest.setCustomerPhone("+1234567890");

        // Assert
        assertEquals("customer@example.com", paymentRequest.getCustomerEmail());
        assertEquals("+1234567890", paymentRequest.getCustomerPhone());
    }

    @Test
    @DisplayName("Should accept callback URL")
    void testCallbackUrl() {
        // Act
        paymentRequest.setCallbackUrl("https://merchant.example.com/webhook");

        // Assert
        assertEquals("https://merchant.example.com/webhook", paymentRequest.getCallbackUrl());
    }

    @Test
    @DisplayName("Should handle reference ID")
    void testReferenceId() {
        // Act
        paymentRequest.setReferenceId("REF-12345");

        // Assert
        assertEquals("REF-12345", paymentRequest.getReferenceId());
    }

    @Test
    @DisplayName("Should handle large amounts")
    void testLargeAmounts() {
        // Test large amount
        BigDecimal largeAmount = new BigDecimal("999999.99");
        paymentRequest.setAmount(largeAmount);

        assertEquals(largeAmount, paymentRequest.getAmount());
    }

    @Test
    @DisplayName("Should handle small amounts")
    void testSmallAmounts() {
        // Test small amount
        BigDecimal smallAmount = new BigDecimal("0.01");
        paymentRequest.setAmount(smallAmount);

        assertEquals(smallAmount, paymentRequest.getAmount());
    }

    @Test
    @DisplayName("Should handle precise decimal amounts")
    void testPreciseDecimalAmounts() {
        // Test precise decimal
        BigDecimal preciseAmount = new BigDecimal("123.4567");
        paymentRequest.setAmount(preciseAmount);

        assertEquals(preciseAmount, paymentRequest.getAmount());
    }
}