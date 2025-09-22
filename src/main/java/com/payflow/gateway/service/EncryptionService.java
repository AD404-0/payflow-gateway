package com.payflow.gateway.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting sensitive payment data
 * Uses AES-256 encryption for PCI DSS compliance
 */
@Service
public class EncryptionService {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int KEY_LENGTH = 256;
    
    private final SecretKey secretKey;
    
    public EncryptionService() {
        this.secretKey = generateSecretKey();
    }
    
    /**
     * Encrypts sensitive data using AES-256
     */
    public String encrypt(String plainText) {
        try {
            if (plainText == null || plainText.isEmpty()) {
                return plainText;
            }
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
            
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }
    
    /**
     * Decrypts encrypted data
     */
    public String decrypt(String encryptedText) {
        try {
            if (encryptedText == null || encryptedText.isEmpty()) {
                return encryptedText;
            }
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);
            
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
    
    /**
     * Generates a secure hash for data integrity verification
     */
    public String hash(String data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing data", e);
        }
    }
    
    /**
     * Generates a random token for API keys, transaction IDs, etc.
     */
    public String generateToken(int length) {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[length];
        random.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes).substring(0, length);
    }
    
    private SecretKey generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating encryption key", e);
        }
    }
}