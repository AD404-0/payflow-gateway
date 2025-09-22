package com.payflow.gateway.service;

import com.payflow.gateway.entity.MerchantEntity;
import com.payflow.gateway.repository.MerchantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for merchant management operations
 */
@Service
@Transactional
public class MerchantService {
    
    private static final Logger logger = LoggerFactory.getLogger(MerchantService.class);
    
    private final MerchantRepository merchantRepository;
    private final EncryptionService encryptionService;
    
    public MerchantService(MerchantRepository merchantRepository, EncryptionService encryptionService) {
        this.merchantRepository = merchantRepository;
        this.encryptionService = encryptionService;
    }
    
    /**
     * Validate if merchant exists and is active
     */
    @Transactional(readOnly = true)
    public boolean isValidMerchant(String merchantId) {
        try {
            Optional<MerchantEntity> merchant = merchantRepository.findById(merchantId);
            return merchant.isPresent() && merchant.get().isActive();
        } catch (Exception e) {
            logger.error("Error validating merchant: {}", merchantId, e);
            return false;
        }
    }
    
    /**
     * Authenticate merchant by API key
     */
    @Transactional(readOnly = true)
    public Optional<MerchantEntity> authenticateByApiKey(String apiKey) {
        try {
            Optional<MerchantEntity> merchant = merchantRepository.findByApiKey(apiKey);
            if (merchant.isPresent() && merchant.get().isActive()) {
                return merchant;
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error authenticating merchant with API key", e);
            return Optional.empty();
        }
    }
    
    /**
     * Create a new merchant
     */
    public MerchantEntity createMerchant(String name, String email, String businessType, String country) {
        try {
            // Check if email already exists
            if (merchantRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Merchant with email already exists: " + email);
            }
            
            MerchantEntity merchant = new MerchantEntity();
            merchant.setId(generateMerchantId());
            merchant.setName(name);
            merchant.setEmail(email);
            merchant.setBusinessType(businessType);
            merchant.setCountry(country);
            merchant.setApiKey(generateApiKey());
            merchant.setApiSecret(generateApiSecret());
            merchant.setStatus("ACTIVE");
            merchant.setIsTestMode(true); // New merchants start in test mode
            merchant.setCreatedAt(LocalDateTime.now());
            merchant.setUpdatedAt(LocalDateTime.now());
            
            return merchantRepository.save(merchant);
            
        } catch (Exception e) {
            logger.error("Error creating merchant", e);
            throw new RuntimeException("Failed to create merchant: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get merchant by ID
     */
    @Transactional(readOnly = true)
    public Optional<MerchantEntity> getMerchant(String merchantId) {
        return merchantRepository.findById(merchantId);
    }
    
    /**
     * Get merchant by email
     */
    @Transactional(readOnly = true)
    public Optional<MerchantEntity> getMerchantByEmail(String email) {
        return merchantRepository.findByEmail(email);
    }
    
    /**
     * Update merchant status
     */
    public void updateMerchantStatus(String merchantId, String status) {
        Optional<MerchantEntity> merchantOpt = merchantRepository.findById(merchantId);
        if (merchantOpt.isPresent()) {
            MerchantEntity merchant = merchantOpt.get();
            merchant.setStatus(status);
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantRepository.save(merchant);
            logger.info("Updated merchant {} status to {}", merchantId, status);
        } else {
            throw new IllegalArgumentException("Merchant not found: " + merchantId);
        }
    }
    
    /**
     * Update merchant webhook URL
     */
    public void updateWebhookUrl(String merchantId, String webhookUrl, String webhookSecret) {
        Optional<MerchantEntity> merchantOpt = merchantRepository.findById(merchantId);
        if (merchantOpt.isPresent()) {
            MerchantEntity merchant = merchantOpt.get();
            merchant.setWebhookUrl(webhookUrl);
            if (webhookSecret != null) {
                merchant.setWebhookSecret(encryptionService.encrypt(webhookSecret));
            }
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantRepository.save(merchant);
            logger.info("Updated webhook URL for merchant {}", merchantId);
        } else {
            throw new IllegalArgumentException("Merchant not found: " + merchantId);
        }
    }
    
    /**
     * Toggle test mode
     */
    public void toggleTestMode(String merchantId, boolean testMode) {
        Optional<MerchantEntity> merchantOpt = merchantRepository.findById(merchantId);
        if (merchantOpt.isPresent()) {
            MerchantEntity merchant = merchantOpt.get();
            merchant.setIsTestMode(testMode);
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantRepository.save(merchant);
            logger.info("Set test mode for merchant {} to {}", merchantId, testMode);
        } else {
            throw new IllegalArgumentException("Merchant not found: " + merchantId);
        }
    }
    
    /**
     * Get all active merchants
     */
    @Transactional(readOnly = true)
    public List<MerchantEntity> getActiveMerchants() {
        return merchantRepository.findActiveMerchants();
    }
    
    /**
     * Get merchant statistics
     */
    @Transactional(readOnly = true)
    public MerchantStatistics getMerchantStatistics() {
        long totalMerchants = merchantRepository.count();
        long activeMerchants = merchantRepository.countByStatus("ACTIVE");
        long inactiveMerchants = merchantRepository.countByStatus("INACTIVE");
        long suspendedMerchants = merchantRepository.countByStatus("SUSPENDED");
        
        return new MerchantStatistics(totalMerchants, activeMerchants, inactiveMerchants, suspendedMerchants);
    }
    
    // Private helper methods
    
    private String generateMerchantId() {
        return "merch_" + encryptionService.generateToken(16);
    }
    
    private String generateApiKey() {
        return "pk_" + encryptionService.generateToken(32);
    }
    
    private String generateApiSecret() {
        return "sk_" + encryptionService.generateToken(32);
    }
    
    /**
     * Statistics about merchants
     */
    public static class MerchantStatistics {
        private final long total;
        private final long active;
        private final long inactive;
        private final long suspended;
        
        public MerchantStatistics(long total, long active, long inactive, long suspended) {
            this.total = total;
            this.active = active;
            this.inactive = inactive;
            this.suspended = suspended;
        }
        
        // Getters
        public long getTotal() { return total; }
        public long getActive() { return active; }
        public long getInactive() { return inactive; }
        public long getSuspended() { return suspended; }
        public double getActivePercentage() { return total > 0 ? (double) active / total * 100 : 0; }
    }
}