package com.payflow.gateway.repository;

import com.payflow.gateway.entity.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for merchant operations
 */
@Repository
public interface MerchantRepository extends JpaRepository<MerchantEntity, String> {
    
    /**
     * Find merchant by API key
     */
    Optional<MerchantEntity> findByApiKey(String apiKey);
    
    /**
     * Find merchant by email
     */
    Optional<MerchantEntity> findByEmail(String email);
    
    /**
     * Find merchants by status
     */
    List<MerchantEntity> findByStatus(String status);
    
    /**
     * Find active merchants
     */
    @Query("SELECT m FROM MerchantEntity m WHERE m.status = 'ACTIVE'")
    List<MerchantEntity> findActiveMerchants();
    
    /**
     * Find merchants created after date
     */
    List<MerchantEntity> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find merchants by business type
     */
    List<MerchantEntity> findByBusinessType(String businessType);
    
    /**
     * Find merchants by country
     */
    List<MerchantEntity> findByCountry(String country);
    
    /**
     * Check if API key exists
     */
    boolean existsByApiKey(String apiKey);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Count merchants by status
     */
    @Query("SELECT COUNT(m) FROM MerchantEntity m WHERE m.status = :status")
    long countByStatus(@Param("status") String status);
}