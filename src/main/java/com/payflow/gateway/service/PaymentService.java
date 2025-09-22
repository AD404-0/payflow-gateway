package com.payflow.gateway.service;

import com.payflow.gateway.command.*;
import com.payflow.gateway.domain.enums.Currency;
import com.payflow.gateway.domain.enums.PaymentMethodType;
import com.payflow.gateway.domain.enums.TransactionStatus;
import com.payflow.gateway.domain.model.*;
import com.payflow.gateway.entity.TransactionEntity;
import com.payflow.gateway.factory.PaymentProcessorFactory;
import com.payflow.gateway.notification.PaymentEvent;
import com.payflow.gateway.notification.PaymentEventPublisher;
import com.payflow.gateway.processor.PaymentProcessor;
import com.payflow.gateway.repository.TransactionRepository;
import com.payflow.gateway.state.TransactionContext;
import com.payflow.gateway.state.StateTransitionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main service orchestrating payment processing
 * Integrates all design patterns: Strategy, Factory, Command, State, Observer
 */
@Service
@Transactional
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentProcessorFactory processorFactory;
    private final PaymentCommandInvoker commandInvoker;
    private final PaymentEventPublisher eventPublisher;
    private final TransactionRepository transactionRepository;
    private final MerchantService merchantService;
    private final EncryptionService encryptionService;
    
    public PaymentService(PaymentProcessorFactory processorFactory,
                         PaymentCommandInvoker commandInvoker,
                         PaymentEventPublisher eventPublisher,
                         TransactionRepository transactionRepository,
                         MerchantService merchantService,
                         EncryptionService encryptionService) {
        this.processorFactory = processorFactory;
        this.commandInvoker = commandInvoker;
        this.eventPublisher = eventPublisher;
        this.transactionRepository = transactionRepository;
        this.merchantService = merchantService;
        this.encryptionService = encryptionService;
    }
    
    /**
     * Process a payment request - Main orchestration method
     */
    public PaymentResult processPayment(PaymentRequest request) {
        logger.info("Processing payment for merchant: {} amount: {}", 
                   request.getMerchantId(), request.getAmount());
        
        try {
            // 1. Validate merchant
            if (!merchantService.isValidMerchant(request.getMerchantId())) {
                return PaymentResult.failure("", "INVALID_MERCHANT", "Merchant not found or inactive");
            }
            
            // 2. Create transaction entity
            TransactionEntity transaction = createTransactionEntity(request);
            transaction = transactionRepository.save(transaction);
            
            // 3. Initialize state machine
            TransactionContext stateContext = new TransactionContext(transaction.getId());
            
            // 4. Get appropriate processor using Factory pattern
            PaymentProcessor processor = processorFactory.getProcessor(request);
            
            // 5. Create and execute command using Command pattern
            Map<String, Object> commandParams = createCommandParameters(request);
            ChargeCommand chargeCommand = new ChargeCommand(
                transaction.getId(), processor, request, commandParams);
            
            CommandResult commandResult = commandInvoker.executeCommand(chargeCommand);
            
            // 6. Update state using State pattern
            StateTransitionResult stateResult;
            if (commandResult.isSuccess()) {
                PaymentResult paymentResult = commandResult.getResultData(PaymentResult.class);
                stateResult = stateContext.complete(paymentResult.getAuthorizationCode());
                updateTransactionFromResult(transaction, paymentResult);
            } else {
                stateResult = stateContext.fail(commandResult.getErrorCode(), commandResult.getMessage());
                transaction.setErrorCode(commandResult.getErrorCode());
                transaction.setErrorMessage(commandResult.getMessage());
            }
            
            // 7. Update transaction in database
            transaction.setStatus(stateContext.getStatus());
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            // 8. Publish events using Observer pattern
            publishPaymentEvent(transaction, stateResult);
            
            // 9. Return result
            if (commandResult.isSuccess()) {
                PaymentResult result = commandResult.getResultData(PaymentResult.class);
                result.setTransactionId(transaction.getId());
                return result;
            } else {
                return PaymentResult.failure(transaction.getId(), 
                                           commandResult.getErrorCode(), commandResult.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment", e);
            return PaymentResult.failure("", "PROCESSING_ERROR", 
                                       "Internal error processing payment: " + e.getMessage());
        }
    }
    
    /**
     * Refund a payment
     */
    public PaymentResult refundPayment(String transactionId, BigDecimal amount, String reason) {
        logger.info("Processing refund for transaction: {} amount: {}", transactionId, amount);
        
        try {
            // 1. Get original transaction
            Optional<TransactionEntity> transactionOpt = transactionRepository.findById(transactionId);
            if (transactionOpt.isEmpty()) {
                return PaymentResult.failure(transactionId, "TRANSACTION_NOT_FOUND", "Transaction not found");
            }
            
            TransactionEntity transaction = transactionOpt.get();
            
            // 2. Validate refund eligibility
            if (!transaction.canBeRefunded()) {
                return PaymentResult.failure(transactionId, "CANNOT_REFUND", 
                                           "Transaction cannot be refunded in current state: " + transaction.getStatus());
            }
            
            // 3. Get processor and create refund command
            PaymentProcessor processor = getProcessorForTransaction(transaction);
            Map<String, Object> commandParams = Map.of("reason", reason, "amount", amount);
            
            RefundCommand refundCommand = new RefundCommand(
                generateRefundTransactionId(), processor, transactionId, amount, reason, commandParams);
            
            // 4. Execute refund command
            CommandResult commandResult = commandInvoker.executeCommand(refundCommand);
            
            // 5. Update state
            TransactionContext stateContext = new TransactionContext(transactionId, transaction.getStatus());
            StateTransitionResult stateResult;
            
            if (commandResult.isSuccess()) {
                // Determine if full or partial refund
                boolean isFullRefund = amount.compareTo(transaction.getAmount()) >= 0;
                if (isFullRefund) {
                    stateResult = stateContext.refund(amount.toString(), reason);
                } else {
                    stateResult = stateContext.partialRefund(amount.toString(), reason);
                }
            } else {
                // Refund failed, transaction stays in current state
                stateResult = StateTransitionResult.failure(transaction.getStatus(), 
                                                           commandResult.getErrorCode(), commandResult.getMessage(), transactionId);
            }
            
            // 6. Update database
            if (stateResult.isSuccess()) {
                transaction.setStatus(stateResult.getToStatus());
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
            }
            
            // 7. Publish refund event
            publishRefundEvent(transaction, stateResult, amount, reason);
            
            // 8. Return result
            if (commandResult.isSuccess()) {
                PaymentResult result = commandResult.getResultData(PaymentResult.class);
                return result;
            } else {
                return PaymentResult.failure(transactionId, commandResult.getErrorCode(), commandResult.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing refund for transaction: {}", transactionId, e);
            return PaymentResult.failure(transactionId, "REFUND_ERROR", 
                                       "Error processing refund: " + e.getMessage());
        }
    }
    
    /**
     * Void/cancel a payment
     */
    public PaymentResult voidPayment(String transactionId, String reason) {
        logger.info("Voiding transaction: {} reason: {}", transactionId, reason);
        
        try {
            Optional<TransactionEntity> transactionOpt = transactionRepository.findById(transactionId);
            if (transactionOpt.isEmpty()) {
                return PaymentResult.failure(transactionId, "TRANSACTION_NOT_FOUND", "Transaction not found");
            }
            
            TransactionEntity transaction = transactionOpt.get();
            TransactionContext stateContext = new TransactionContext(transactionId, transaction.getStatus());
            
            // Check if transaction can be voided
            if (!stateContext.canTransitionTo(TransactionStatus.CANCELLED)) {
                return PaymentResult.failure(transactionId, "CANNOT_VOID", 
                                           "Transaction cannot be voided in current state: " + transaction.getStatus());
            }
            
            // Get processor and create void command
            PaymentProcessor processor = getProcessorForTransaction(transaction);
            Map<String, Object> commandParams = Map.of("reason", reason);
            
            VoidCommand voidCommand = new VoidCommand(
                generateVoidTransactionId(), processor, transactionId, reason, commandParams);
            
            // Execute void command
            CommandResult commandResult = commandInvoker.executeCommand(voidCommand);
            
            // Update state
            StateTransitionResult stateResult;
            if (commandResult.isSuccess()) {
                stateResult = stateContext.cancel(reason);
            } else {
                stateResult = StateTransitionResult.failure(transaction.getStatus(), 
                                                           commandResult.getErrorCode(), commandResult.getMessage(), transactionId);
            }
            
            // Update database
            if (stateResult.isSuccess()) {
                transaction.setStatus(stateResult.getToStatus());
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
            }
            
            // Publish void event
            publishVoidEvent(transaction, stateResult, reason);
            
            return commandResult.isSuccess() ? 
                commandResult.getResultData(PaymentResult.class) :
                PaymentResult.failure(transactionId, commandResult.getErrorCode(), commandResult.getMessage());
                
        } catch (Exception e) {
            logger.error("Error voiding transaction: {}", transactionId, e);
            return PaymentResult.failure(transactionId, "VOID_ERROR", 
                                       "Error voiding transaction: " + e.getMessage());
        }
    }
    
    /**
     * Get transaction by ID with full details
     */
    @Transactional(readOnly = true)
    public Optional<TransactionEntity> getTransaction(String transactionId) {
        return transactionRepository.findById(transactionId);
    }
    
    /**
     * Get transactions for a merchant with pagination
     */
    @Transactional(readOnly = true)
    public List<TransactionEntity> getMerchantTransactions(String merchantId) {
        return transactionRepository.findByMerchantId(merchantId);
    }
    
    /**
     * Get transaction statistics for a merchant
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMerchantStatistics(String merchantId, LocalDateTime fromDate, LocalDateTime toDate) {
        Map<String, Object> stats = new HashMap<>();
        
        // Count by status
        for (TransactionStatus status : TransactionStatus.values()) {
            long count = transactionRepository.countByMerchantIdAndStatus(merchantId, status);
            stats.put(status.getCode() + "_count", count);
        }
        
        // Total amounts
        BigDecimal completedAmount = transactionRepository.getTotalAmountByMerchantAndStatus(
            merchantId, TransactionStatus.COMPLETED);
        BigDecimal refundedAmount = transactionRepository.getTotalAmountByMerchantAndStatus(
            merchantId, TransactionStatus.REFUNDED);
        
        stats.put("total_completed_amount", completedAmount);
        stats.put("total_refunded_amount", refundedAmount);
        stats.put("net_amount", completedAmount.subtract(refundedAmount));
        
        return stats;
    }
    
    // Private helper methods
    
    private TransactionEntity createTransactionEntity(PaymentRequest request) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setId(generateTransactionId());
        transaction.setMerchantId(request.getMerchantId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setReferenceId(request.getReferenceId());
        transaction.setDescription(request.getDescription());
        transaction.setCustomerEmail(request.getCustomerEmail());
        transaction.setCustomerPhone(request.getCustomerPhone());
        transaction.setWebhookUrl(request.getWebhookUrl());
        transaction.setCallbackUrl(request.getCallbackUrl());
        transaction.setExpiresAt(request.getExpiresAt());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        return transaction;
    }
    
    private void updateTransactionFromResult(TransactionEntity transaction, PaymentResult result) {
        transaction.setAuthorizationCode(result.getAuthorizationCode());
        transaction.setProcessorTransactionId(result.getProcessorTransactionId());
        transaction.setProcessorResponse(result.getProcessorResponse());
        transaction.setProcessedAt(LocalDateTime.now());
    }
    
    private Map<String, Object> createCommandParameters(PaymentRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("merchant_id", request.getMerchantId());
        params.put("reference_id", request.getReferenceId());
        params.put("webhook_url", request.getWebhookUrl());
        return params;
    }
    
    private PaymentProcessor getProcessorForTransaction(TransactionEntity transaction) {
        // In a real implementation, this would be stored with the transaction
        // For now, we'll make an educated guess based on transaction data
        PaymentMethodType methodType = PaymentMethodType.CREDIT_CARD; // Default
        return processorFactory.getProcessor(methodType);
    }
    
    private void publishPaymentEvent(TransactionEntity transaction, StateTransitionResult stateResult) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("amount", transaction.getAmount());
        eventData.put("currency", transaction.getCurrency().getCode());
        eventData.put("webhook_url", transaction.getWebhookUrl());
        
        PaymentEvent event;
        if (stateResult.isSuccess() && stateResult.getToStatus() == TransactionStatus.COMPLETED) {
            event = PaymentEvent.paymentCompleted(transaction.getId(), transaction.getMerchantId(), eventData);
        } else if (!stateResult.isSuccess() || stateResult.getToStatus() == TransactionStatus.FAILED) {
            event = PaymentEvent.paymentFailed(transaction.getId(), transaction.getMerchantId(), eventData);
        } else {
            event = PaymentEvent.transactionStatusChanged(transaction.getId(), transaction.getMerchantId(),
                                                        stateResult.getToStatus(), stateResult.getFromStatus(), eventData);
        }
        
        eventPublisher.publishEvent(event);
    }
    
    private void publishRefundEvent(TransactionEntity transaction, StateTransitionResult stateResult, 
                                   BigDecimal amount, String reason) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("refund_amount", amount);
        eventData.put("reason", reason);
        eventData.put("webhook_url", transaction.getWebhookUrl());
        
        if (stateResult.isSuccess()) {
            PaymentEvent event = PaymentEvent.refundProcessed(transaction.getId(), transaction.getMerchantId(), eventData);
            eventPublisher.publishEvent(event);
        }
    }
    
    private void publishVoidEvent(TransactionEntity transaction, StateTransitionResult stateResult, String reason) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("reason", reason);
        eventData.put("webhook_url", transaction.getWebhookUrl());
        
        if (stateResult.isSuccess()) {
            PaymentEvent event = PaymentEvent.transactionStatusChanged(
                transaction.getId(), transaction.getMerchantId(),
                TransactionStatus.CANCELLED, stateResult.getFromStatus(), eventData);
            eventPublisher.publishEvent(event);
        }
    }
    
    private String generateTransactionId() {
        return "txn_" + encryptionService.generateToken(16);
    }
    
    private String generateRefundTransactionId() {
        return "rfnd_" + encryptionService.generateToken(16);
    }
    
    private String generateVoidTransactionId() {
        return "void_" + encryptionService.generateToken(16);
    }
}