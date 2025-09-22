package com.payflow.gateway.repository;

import com.payflow.gateway.domain.enums.TransactionStatus;
import com.payflow.gateway.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for transaction operations
 * Provides advanced querying capabilities for payment transactions
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
    
    /**
     * Find transactions by merchant ID
     */
    List<TransactionEntity> findByMerchantId(String merchantId);
    
    /**
     * Find transactions by merchant ID with pagination
     */
    Page<TransactionEntity> findByMerchantId(String merchantId, Pageable pageable);
    
    /**
     * Find transactions by status
     */
    List<TransactionEntity> findByStatus(TransactionStatus status);
    
    /**
     * Find transactions by merchant and status
     */
    List<TransactionEntity> findByMerchantIdAndStatus(String merchantId, TransactionStatus status);
    
    /**
     * Find transaction by reference ID
     */
    Optional<TransactionEntity> findByReferenceId(String referenceId);
    
    /**
     * Find transactions by processor transaction ID
     */
    Optional<TransactionEntity> findByProcessorTransactionId(String processorTransactionId);
    
    /**
     * Find transactions within date range
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<TransactionEntity> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find transactions by merchant within date range
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.merchantId = :merchantId AND t.createdAt BETWEEN :startDate AND :endDate")
    Page<TransactionEntity> findByMerchantIdAndDateRange(@Param("merchantId") String merchantId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate,
                                                        Pageable pageable);
    
    /**
     * Find transactions by amount range
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.amount BETWEEN :minAmount AND :maxAmount")
    List<TransactionEntity> findByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                             @Param("maxAmount") BigDecimal maxAmount);
    
    /**
     * Count transactions by merchant and status
     */
    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.merchantId = :merchantId AND t.status = :status")
    long countByMerchantIdAndStatus(@Param("merchantId") String merchantId, 
                                   @Param("status") TransactionStatus status);
    
    /**
     * Get total transaction amount by merchant
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.merchantId = :merchantId AND t.status = :status")
    BigDecimal getTotalAmountByMerchantAndStatus(@Param("merchantId") String merchantId, 
                                                @Param("status") TransactionStatus status);
    
    /**
     * Find failed transactions for retry
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.status = 'FAILED' AND t.errorCode NOT IN ('DECLINED', 'FRAUD_DETECTED') AND t.createdAt > :cutoffDate")
    List<TransactionEntity> findFailedTransactionsForRetry(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find expired pending transactions
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.status = 'PENDING' AND t.expiresAt < :currentTime")
    List<TransactionEntity> findExpiredPendingTransactions(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find transactions by customer email
     */
    List<TransactionEntity> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
    
    /**
     * Update transaction status
     */
    @Modifying
    @Query("UPDATE TransactionEntity t SET t.status = :status, t.updatedAt = :updatedAt WHERE t.id = :transactionId")
    int updateTransactionStatus(@Param("transactionId") String transactionId, 
                               @Param("status") TransactionStatus status,
                               @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Mark transaction as processed
     */
    @Modifying
    @Query("UPDATE TransactionEntity t SET t.status = :status, t.processedAt = :processedAt, t.authorizationCode = :authCode, t.updatedAt = :updatedAt WHERE t.id = :transactionId")
    int markTransactionAsProcessed(@Param("transactionId") String transactionId,
                                  @Param("status") TransactionStatus status,
                                  @Param("processedAt") LocalDateTime processedAt,
                                  @Param("authCode") String authCode,
                                  @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Get daily transaction summary
     */
    @Query("""
        SELECT DATE(t.createdAt) as date, 
               t.status as status, 
               COUNT(t) as count, 
               SUM(t.amount) as total_amount 
        FROM TransactionEntity t 
        WHERE t.merchantId = :merchantId 
          AND t.createdAt >= :startDate 
        GROUP BY DATE(t.createdAt), t.status 
        ORDER BY DATE(t.createdAt) DESC
        """)
    List<Object[]> getDailyTransactionSummary(@Param("merchantId") String merchantId, 
                                             @Param("startDate") LocalDateTime startDate);
    
    /**
     * Check if reference ID exists for merchant
     */
    boolean existsByMerchantIdAndReferenceId(String merchantId, String referenceId);
}