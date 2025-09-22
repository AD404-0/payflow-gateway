package com.payflow.gateway.controller;

import com.payflow.gateway.domain.model.PaymentRequest;
import com.payflow.gateway.domain.model.PaymentResult;
import com.payflow.gateway.entity.TransactionEntity;
import com.payflow.gateway.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API controller for payment operations
 * Provides endpoints for payment processing, refunds, and transaction management
 */
@RestController
@RequestMapping("/payments")
@Tag(name = "Payment Operations", description = "APIs for processing payments, refunds, and managing transactions")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentService paymentService;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    /**
     * Process a new payment
     */
    @PostMapping("/process")
    @Operation(summary = "Process Payment", description = "Process a new payment transaction")
    public ResponseEntity<ApiResponse<PaymentResult>> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            logger.info("Processing payment request for merchant: {}", request.getMerchantId());
            
            PaymentResult result = paymentService.processPayment(request);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(result, "Payment processed successfully"));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getErrorCode(), result.getErrorMessage(), result));
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("PROCESSING_ERROR", "Internal server error", null));
        }
    }
    
    /**
     * Refund a payment
     */
    @PostMapping("/{transactionId}/refund")
    @Operation(summary = "Refund Payment", description = "Refund a completed payment transaction")
    public ResponseEntity<ApiResponse<PaymentResult>> refundPayment(
            @Parameter(description = "Transaction ID to refund") @PathVariable String transactionId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam String reason,
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            logger.info("Processing refund for transaction: {} amount: {}", transactionId, amount);
            
            PaymentResult result = paymentService.refundPayment(transactionId, amount, reason);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(result, "Refund processed successfully"));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getErrorCode(), result.getErrorMessage(), result));
            }
            
        } catch (Exception e) {
            logger.error("Error processing refund for transaction: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("REFUND_ERROR", "Internal server error", null));
        }
    }
    
    /**
     * Void/cancel a payment
     */
    @PostMapping("/{transactionId}/void")
    @Operation(summary = "Void Payment", description = "Cancel a payment transaction")
    public ResponseEntity<ApiResponse<PaymentResult>> voidPayment(
            @Parameter(description = "Transaction ID to void") @PathVariable String transactionId,
            @RequestParam String reason,
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            logger.info("Voiding transaction: {} reason: {}", transactionId, reason);
            
            PaymentResult result = paymentService.voidPayment(transactionId, reason);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(result, "Transaction voided successfully"));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getErrorCode(), result.getErrorMessage(), result));
            }
            
        } catch (Exception e) {
            logger.error("Error voiding transaction: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("VOID_ERROR", "Internal server error", null));
        }
    }
    
    /**
     * Get transaction details
     */
    @GetMapping("/{transactionId}")
    @Operation(summary = "Get Transaction", description = "Retrieve transaction details by ID")
    public ResponseEntity<ApiResponse<TransactionEntity>> getTransaction(
            @Parameter(description = "Transaction ID") @PathVariable String transactionId,
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            Optional<TransactionEntity> transaction = paymentService.getTransaction(transactionId);
            
            if (transaction.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(transaction.get(), "Transaction found"));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error getting transaction: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("RETRIEVAL_ERROR", "Internal server error", null));
        }
    }
    
    /**
     * Get merchant transactions
     */
    @GetMapping("/merchant/{merchantId}")
    @Operation(summary = "Get Merchant Transactions", description = "Retrieve all transactions for a merchant")
    public ResponseEntity<ApiResponse<List<TransactionEntity>>> getMerchantTransactions(
            @Parameter(description = "Merchant ID") @PathVariable String merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            List<TransactionEntity> transactions = paymentService.getMerchantTransactions(merchantId);
            
            return ResponseEntity.ok(ApiResponse.success(transactions, "Transactions retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error getting merchant transactions: {}", merchantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("RETRIEVAL_ERROR", "Internal server error", null));
        }
    }
    
    /**
     * Get merchant statistics
     */
    @GetMapping("/merchant/{merchantId}/statistics")
    @Operation(summary = "Get Merchant Statistics", description = "Retrieve transaction statistics for a merchant")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMerchantStatistics(
            @Parameter(description = "Merchant ID") @PathVariable String merchantId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            LocalDateTime from = fromDate != null ? LocalDateTime.parse(fromDate) : LocalDateTime.now().minusMonths(1);
            LocalDateTime to = toDate != null ? LocalDateTime.parse(toDate) : LocalDateTime.now();
            
            Map<String, Object> statistics = paymentService.getMerchantStatistics(merchantId, from, to);
            
            return ResponseEntity.ok(ApiResponse.success(statistics, "Statistics retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error getting merchant statistics: {}", merchantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("STATISTICS_ERROR", "Internal server error", null));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check if the payment service is healthy")
    public ResponseEntity<ApiResponse<Map<String, String>>> healthCheck() {
        Map<String, String> health = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now().toString(),
            "service", "PayFlow Gateway"
        );
        
        return ResponseEntity.ok(ApiResponse.success(health, "Service is healthy"));
    }
}