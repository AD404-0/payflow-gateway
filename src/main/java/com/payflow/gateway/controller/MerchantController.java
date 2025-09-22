package com.payflow.gateway.controller;

import com.payflow.gateway.entity.MerchantEntity;
import com.payflow.gateway.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST API controller for merchant management operations
 */
@RestController
@RequestMapping("/merchants")
@Tag(name = "Merchant Management", description = "APIs for managing merchants and their configurations")
public class MerchantController {
    
    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);
    
    private final MerchantService merchantService;
    
    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }
    
    /**
     * Create a new merchant
     */
    @PostMapping
    @Operation(summary = "Create Merchant", description = "Create a new merchant account")
    public ResponseEntity<ApiResponse<MerchantEntity>> createMerchant(
            @Valid @RequestBody CreateMerchantRequest request) {
        
        try {
            logger.info("Creating new merchant: {}", request.getEmail());
            
            MerchantEntity merchant = merchantService.createMerchant(
                request.getName(), 
                request.getEmail(), 
                request.getBusinessType(), 
                request.getCountry()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(merchant, "Merchant created successfully"));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error creating merchant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("CREATION_ERROR", "Internal server error", null));
        }
    }
    
    /**
     * Get merchant by ID
     */
    @GetMapping("/{merchantId}")
    @Operation(summary = "Get Merchant", description = "Retrieve merchant details by ID")
    public ResponseEntity<ApiResponse<MerchantEntity>> getMerchant(
            @Parameter(description = "Merchant ID") @PathVariable String merchantId,
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            Optional<MerchantEntity> merchant = merchantService.getMerchant(merchantId);
            
            if (merchant.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(merchant.get(), "Merchant found"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("Merchant not found"));
            }
            
        } catch (Exception e) {
            logger.error("Error getting merchant: {}", merchantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("RETRIEVAL_ERROR", "Internal server error", null));
        }
    }
    
    /**
     * Update merchant status
     */
    @PutMapping("/{merchantId}/status")
    @Operation(summary = "Update Merchant Status", description = "Update merchant account status")
    public ResponseEntity<ApiResponse<Void>> updateMerchantStatus(
            @Parameter(description = "Merchant ID") @PathVariable String merchantId,
            @RequestParam String status,
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            merchantService.updateMerchantStatus(merchantId, status);
            return ResponseEntity.ok(ApiResponse.success("Merchant status updated successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating merchant status: {}", merchantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("UPDATE_ERROR", "Internal server error"));
        }
    }
    
    /**
     * Update webhook configuration
     */
    @PutMapping("/{merchantId}/webhook")
    @Operation(summary = "Update Webhook", description = "Update merchant webhook configuration")
    public ResponseEntity<ApiResponse<Void>> updateWebhook(
            @Parameter(description = "Merchant ID") @PathVariable String merchantId,
            @Valid @RequestBody WebhookUpdateRequest request,
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            merchantService.updateWebhookUrl(merchantId, request.getWebhookUrl(), request.getWebhookSecret());
            return ResponseEntity.ok(ApiResponse.success("Webhook configuration updated successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating webhook for merchant: {}", merchantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("UPDATE_ERROR", "Internal server error"));
        }
    }
    
    /**
     * Toggle test mode
     */
    @PutMapping("/{merchantId}/test-mode")
    @Operation(summary = "Toggle Test Mode", description = "Enable or disable test mode for merchant")
    public ResponseEntity<ApiResponse<Void>> toggleTestMode(
            @Parameter(description = "Merchant ID") @PathVariable String merchantId,
            @RequestParam boolean testMode,
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            merchantService.toggleTestMode(merchantId, testMode);
            return ResponseEntity.ok(ApiResponse.success("Test mode updated successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating test mode for merchant: {}", merchantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("UPDATE_ERROR", "Internal server error"));
        }
    }
    
    /**
     * Get all active merchants
     */
    @GetMapping("/active")
    @Operation(summary = "Get Active Merchants", description = "Retrieve all active merchants")
    public ResponseEntity<ApiResponse<List<MerchantEntity>>> getActiveMerchants(
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            List<MerchantEntity> merchants = merchantService.getActiveMerchants();
            return ResponseEntity.ok(ApiResponse.success(merchants, "Active merchants retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error getting active merchants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("RETRIEVAL_ERROR", "Internal server error", null));
        }
    }
    
    /**
     * Get merchant statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get Merchant Statistics", description = "Retrieve overall merchant statistics")
    public ResponseEntity<ApiResponse<MerchantService.MerchantStatistics>> getMerchantStatistics(
            @RequestHeader("X-API-Key") String apiKey) {
        
        try {
            MerchantService.MerchantStatistics stats = merchantService.getMerchantStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error getting merchant statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("STATISTICS_ERROR", "Internal server error", null));
        }
    }
    
    /**
     * Request DTO for creating merchants
     */
    public static class CreateMerchantRequest {
        private String name;
        private String email;
        private String businessType;
        private String country;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }
    
    /**
     * Request DTO for webhook updates
     */
    public static class WebhookUpdateRequest {
        private String webhookUrl;
        private String webhookSecret;
        
        // Getters and setters
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
        public String getWebhookSecret() { return webhookSecret; }
        public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    }
}