package com.payflow.gateway.factory;

import com.payflow.gateway.domain.enums.PaymentMethodType;
import com.payflow.gateway.domain.model.PaymentRequest;
import com.payflow.gateway.processor.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory class for creating appropriate payment processors
 * Implements the Factory Pattern for payment method selection
 */
@Component
public class PaymentProcessorFactory {
    
    private final Map<PaymentMethodType, PaymentProcessor> processorMap;
    private final List<PaymentProcessor> allProcessors;
    
    public PaymentProcessorFactory(CreditCardProcessor creditCardProcessor,
                                 BankTransferProcessor bankTransferProcessor,
                                 DigitalWalletProcessor digitalWalletProcessor) {
        
        this.allProcessors = List.of(creditCardProcessor, bankTransferProcessor, digitalWalletProcessor);
        
        this.processorMap = Map.of(
            PaymentMethodType.CREDIT_CARD, creditCardProcessor,
            PaymentMethodType.DEBIT_CARD, creditCardProcessor,
            PaymentMethodType.BANK_TRANSFER, bankTransferProcessor,
            PaymentMethodType.DIGITAL_WALLET, digitalWalletProcessor
        );
    }
    
    /**
     * Gets the appropriate payment processor for a given payment method type
     */
    public PaymentProcessor getProcessor(PaymentMethodType paymentMethodType) {
        PaymentProcessor processor = processorMap.get(paymentMethodType);
        
        if (processor == null) {
            throw new UnsupportedPaymentMethodException(
                "No processor available for payment method: " + paymentMethodType);
        }
        
        return processor;
    }
    
    /**
     * Gets the appropriate payment processor for a payment request
     * Uses dynamic selection based on request validation
     */
    public PaymentProcessor getProcessor(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request cannot be null");
        }
        
        if (request.getPaymentMethodType() == null) {
            throw new IllegalArgumentException("Payment method type is required");
        }
        
        // First try direct mapping
        PaymentProcessor processor = processorMap.get(request.getPaymentMethodType());
        
        // If not found or processor cannot handle the request, search dynamically
        if (processor == null || !processor.canProcess(request)) {
            processor = findCompatibleProcessor(request);
        }
        
        if (processor == null) {
            throw new UnsupportedPaymentMethodException(
                "No compatible processor found for payment request with method: " + 
                request.getPaymentMethodType());
        }
        
        return processor;
    }
    
    /**
     * Gets all available payment processors
     */
    public List<PaymentProcessor> getAllProcessors() {
        return List.copyOf(allProcessors);
    }
    
    /**
     * Gets supported payment method types
     */
    public List<PaymentMethodType> getSupportedPaymentMethods() {
        return List.copyOf(processorMap.keySet());
    }
    
    /**
     * Checks if a payment method type is supported
     */
    public boolean isPaymentMethodSupported(PaymentMethodType paymentMethodType) {
        return processorMap.containsKey(paymentMethodType);
    }
    
    /**
     * Gets processor capabilities information
     */
    public Map<String, String> getProcessorCapabilities() {
        return allProcessors.stream()
            .collect(Collectors.toMap(
                processor -> processor.getClass().getSimpleName(),
                PaymentProcessor::getSupportedPaymentMethodType
            ));
    }
    
    /**
     * Dynamically finds a compatible processor for the request
     */
    private PaymentProcessor findCompatibleProcessor(PaymentRequest request) {
        return allProcessors.stream()
            .filter(processor -> processor.canProcess(request))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Custom exception for unsupported payment methods
     */
    public static class UnsupportedPaymentMethodException extends RuntimeException {
        public UnsupportedPaymentMethodException(String message) {
            super(message);
        }
        
        public UnsupportedPaymentMethodException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}