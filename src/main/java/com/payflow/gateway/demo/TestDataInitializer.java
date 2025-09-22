package com.payflow.gateway.demo;

import com.payflow.gateway.entity.MerchantEntity;
import com.payflow.gateway.entity.TransactionEntity;
import com.payflow.gateway.repository.MerchantRepository;
import com.payflow.gateway.repository.TransactionRepository;
import com.payflow.gateway.service.EncryptionService;
import com.payflow.gateway.domain.enums.Currency;
import com.payflow.gateway.domain.enums.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Test data initializer for PayFlow Gateway
 * Creates fake merchants and transactions for testing
 */
@Component
@Order(2) // Run after SecurityDemoRunner
public class TestDataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDataInitializer.class);
    
    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final EncryptionService encryptionService;
    
    public TestDataInitializer(MerchantRepository merchantRepository, 
                              TransactionRepository transactionRepository,
                              EncryptionService encryptionService) {
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
        this.encryptionService = encryptionService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Initializing Test Data ===");
        
        // Check if test data already exists
        if (merchantRepository.count() > 0) {
            logger.info("Test data already exists, skipping initialization");
            logExistingMerchants();
            return;
        }
        
        createTestMerchants();
        createTestTransactions();
        
        logger.info("=== Test Data Initialization Complete ===");
        logExistingMerchants();
    }
    
    private void createTestMerchants() {
        logger.info("Creating test merchants...");
        
        List<TestMerchantData> testMerchants = Arrays.asList(
            new TestMerchantData("test-merchant-1", "Test E-commerce Store", "ecommerce@test.com", 
                                "pk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "sk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", 
                                "E-COMMERCE", "USA"),
            new TestMerchantData("demo-merchant-123", "Demo Payment Processor", "demo@payflow.com", 
                                "pk_demo_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "sk_demo_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", 
                                "PAYMENT_PROCESSOR", "USA"),
            new TestMerchantData("test-merchant-2", "Sample Online Store", "store@example.com", 
                                "pk_test_YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY", "sk_test_YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY", 
                                "RETAIL", "CAN"),
            new TestMerchantData("api-test-merchant", "API Testing Company", "api@testcompany.com", 
                                "pk_test_ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", "sk_test_ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", 
                                "SOFTWARE", "GBR")
        );
        
        for (TestMerchantData data : testMerchants) {
            MerchantEntity merchant = new MerchantEntity();
            merchant.setId(data.id);
            merchant.setName(data.name);
            merchant.setEmail(data.email);
            merchant.setApiKey(data.apiKey);
            merchant.setApiSecret(data.apiSecret);
            merchant.setBusinessType(data.businessType);
            merchant.setCountry(data.country);
            merchant.setStatus("ACTIVE");
            merchant.setIsTestMode(true);
            merchant.setCreatedAt(LocalDateTime.now());
            merchant.setUpdatedAt(LocalDateTime.now());
            merchant.setWebhookUrl("https://webhook.test.com/" + data.id);
            merchant.setWebhookSecret(encryptionService.generateToken(32));
            
            merchantRepository.save(merchant);
            logger.info("Created merchant: {} with API key: {}", data.name, maskApiKey(data.apiKey));
        }
    }
    
    private void createTestTransactions() {
        logger.info("Creating test transactions...");
        
        List<MerchantEntity> merchants = merchantRepository.findAll();
        
        for (MerchantEntity merchant : merchants) {
            // Create sample transactions for each merchant
            createTransactionForMerchant(merchant, "txn_001_" + merchant.getId(), 
                                       new BigDecimal("99.99"), Currency.USD, TransactionStatus.COMPLETED);
            createTransactionForMerchant(merchant, "txn_002_" + merchant.getId(), 
                                       new BigDecimal("149.50"), Currency.USD, TransactionStatus.PENDING);
            createTransactionForMerchant(merchant, "txn_003_" + merchant.getId(), 
                                       new BigDecimal("75.25"), Currency.EUR, TransactionStatus.FAILED);
        }
    }
    
    private void createTransactionForMerchant(MerchantEntity merchant, String transactionId, 
                                            BigDecimal amount, Currency currency, TransactionStatus status) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setId(transactionId);
        transaction.setMerchant(merchant);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setStatus(status);
        transaction.setDescription("Test transaction for " + merchant.getName());
        transaction.setCustomerEmail("customer@test.com");
        transaction.setReferenceId("ref_" + transactionId);
        transaction.setCreatedAt(LocalDateTime.now().minusDays((long) (Math.random() * 30)));
        transaction.setUpdatedAt(LocalDateTime.now());
        
        if ("COMPLETED".equals(status.toString())) {
            transaction.setProcessedAt(LocalDateTime.now().minusHours((long) (Math.random() * 24)));
            transaction.setAuthorizationCode("AUTH_" + encryptionService.generateToken(8));
        }
        
        transactionRepository.save(transaction);
    }
    
    private void logExistingMerchants() {
        logger.info("=== Available Test Merchants ===");
        List<MerchantEntity> merchants = merchantRepository.findAll();
        
        for (MerchantEntity merchant : merchants) {
            logger.info("Merchant: {} | API Key: {} | Status: {}", 
                       merchant.getName(), maskApiKey(merchant.getApiKey()), merchant.getStatus());
        }
        
        logger.info("=== API Usage Examples ===");
        if (!merchants.isEmpty()) {
            MerchantEntity firstMerchant = merchants.get(0);
            logger.info("Example API calls using merchant '{}' with API key '{}':", 
                       firstMerchant.getName(), maskApiKey(firstMerchant.getApiKey()));
            logger.info("  curl -H \"X-API-Key: {}\" http://localhost:8080/api/v1/merchants/{}", 
                       maskApiKey(firstMerchant.getApiKey()), firstMerchant.getId());
            logger.info("  curl -H \"Authorization: Bearer {}\" http://localhost:8080/api/v1/payments", 
                       maskApiKey(firstMerchant.getApiKey()));
        }
    }
    
    private static class TestMerchantData {
        final String id;
        final String name;
        final String email;
        final String apiKey;
        final String apiSecret;
        final String businessType;
        final String country;
        
        TestMerchantData(String id, String name, String email, String apiKey, String apiSecret, 
                        String businessType, String country) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
            this.businessType = businessType;
            this.country = country;
        }
    }
    
    /**
     * Masks API key for safe logging
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}