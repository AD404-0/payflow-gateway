package com.payflow.gateway.domain.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Currency Enum Tests")
public class CurrencyTest {

    @Test
    @DisplayName("Should have all major currencies defined")
    void testMajorCurrencies() {
        // Test that major currencies are defined
        assertNotNull(Currency.USD);
        assertNotNull(Currency.EUR);
        assertNotNull(Currency.GBP);
        assertNotNull(Currency.JPY);
        assertNotNull(Currency.CAD);
        assertNotNull(Currency.AUD);
    }

    @Test
    @DisplayName("Should return correct string representation")
    void testStringRepresentation() {
        assertEquals("USD", Currency.USD.toString());
        assertEquals("EUR", Currency.EUR.toString());
        assertEquals("GBP", Currency.GBP.toString());
        assertEquals("JPY", Currency.JPY.toString());
    }

    @Test
    @DisplayName("Should support valueOf operation")
    void testValueOf() {
        assertEquals(Currency.USD, Currency.valueOf("USD"));
        assertEquals(Currency.EUR, Currency.valueOf("EUR"));
        assertEquals(Currency.GBP, Currency.valueOf("GBP"));
    }

    @Test
    @DisplayName("Should throw exception for invalid currency")
    void testInvalidCurrency() {
        assertThrows(IllegalArgumentException.class, () -> {
            Currency.valueOf("INVALID");
        });
    }

    @Test
    @DisplayName("Should have consistent values() method")
    void testValuesMethod() {
        Currency[] currencies = Currency.values();
        
        assertNotNull(currencies);
        assertTrue(currencies.length > 0);
        
        // Check that USD is in the array
        boolean usdFound = false;
        for (Currency currency : currencies) {
            if (currency == Currency.USD) {
                usdFound = true;
                break;
            }
        }
        assertTrue(usdFound, "USD should be in the values array");
    }
}

@DisplayName("Transaction Status Enum Tests")
class TransactionStatusTest {

    @Test
    @DisplayName("Should have all transaction statuses defined")
    void testTransactionStatuses() {
        assertNotNull(TransactionStatus.PENDING);
        assertNotNull(TransactionStatus.PROCESSING);
        assertNotNull(TransactionStatus.COMPLETED);
        assertNotNull(TransactionStatus.FAILED);
        assertNotNull(TransactionStatus.CANCELLED);
        assertNotNull(TransactionStatus.REFUNDED);
    }

    @Test
    @DisplayName("Should return correct string representation")
    void testStringRepresentation() {
        assertEquals("PENDING", TransactionStatus.PENDING.toString());
        assertEquals("PROCESSING", TransactionStatus.PROCESSING.toString());
        assertEquals("COMPLETED", TransactionStatus.COMPLETED.toString());
        assertEquals("FAILED", TransactionStatus.FAILED.toString());
    }

    @Test
    @DisplayName("Should support valueOf operation")
    void testValueOf() {
        assertEquals(TransactionStatus.PENDING, TransactionStatus.valueOf("PENDING"));
        assertEquals(TransactionStatus.COMPLETED, TransactionStatus.valueOf("COMPLETED"));
        assertEquals(TransactionStatus.FAILED, TransactionStatus.valueOf("FAILED"));
    }
}

@DisplayName("Payment Method Type Enum Tests")
class PaymentMethodTypeTest {

    @Test
    @DisplayName("Should have all payment method types defined")
    void testPaymentMethodTypes() {
        assertNotNull(PaymentMethodType.CREDIT_CARD);
        assertNotNull(PaymentMethodType.DEBIT_CARD);
        assertNotNull(PaymentMethodType.BANK_TRANSFER);
        assertNotNull(PaymentMethodType.DIGITAL_WALLET);
    }

    @Test
    @DisplayName("Should return correct string representation")
    void testStringRepresentation() {
        assertEquals("CREDIT_CARD", PaymentMethodType.CREDIT_CARD.toString());
        assertEquals("DEBIT_CARD", PaymentMethodType.DEBIT_CARD.toString());
        assertEquals("BANK_TRANSFER", PaymentMethodType.BANK_TRANSFER.toString());
        assertEquals("DIGITAL_WALLET", PaymentMethodType.DIGITAL_WALLET.toString());
    }

    @Test
    @DisplayName("Should support valueOf operation")
    void testValueOf() {
        assertEquals(PaymentMethodType.CREDIT_CARD, PaymentMethodType.valueOf("CREDIT_CARD"));
        assertEquals(PaymentMethodType.BANK_TRANSFER, PaymentMethodType.valueOf("BANK_TRANSFER"));
        assertEquals(PaymentMethodType.DIGITAL_WALLET, PaymentMethodType.valueOf("DIGITAL_WALLET"));
    }

    @Test
    @DisplayName("Should handle card types correctly")
    void testCardTypes() {
        // Both credit and debit cards should be available
        assertNotEquals(PaymentMethodType.CREDIT_CARD, PaymentMethodType.DEBIT_CARD);
        
        // Test that they are both card-related types
        assertTrue(PaymentMethodType.CREDIT_CARD.toString().contains("CARD"));
        assertTrue(PaymentMethodType.DEBIT_CARD.toString().contains("CARD"));
    }
}