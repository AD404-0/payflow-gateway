package com.payflow.gateway.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper for all PayFlow API endpoints
 * Provides consistent response structure across all APIs
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {
    
    @Schema(description = "Indicates if the request was successful", example = "true")
    private boolean success;
    
    @Schema(description = "Human-readable message describing the result", example = "Payment processed successfully")
    private String message;
    
    @Schema(description = "Error code if the request failed", example = "INVALID_CARD")
    private String errorCode;
    
    @Schema(description = "Response data payload")
    private T data;
    
    @Schema(description = "Response timestamp", example = "2024-09-18T14:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "API version", example = "v1")
    private String version = "v1";
    
    // Private constructor to enforce factory methods
    private ApiResponse(boolean success, String message, String errorCode, T data) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Create a successful response with data
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, null, data);
    }
    
    /**
     * Create a successful response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null);
    }
    
    /**
     * Create an error response with data
     */
    public static <T> ApiResponse<T> error(String errorCode, String message, T data) {
        return new ApiResponse<>(false, message, errorCode, data);
    }
    
    /**
     * Create an error response without data
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, message, errorCode, null);
    }
    
    /**
     * Create a validation error response
     */
    public static <T> ApiResponse<T> validationError(String message, T validationDetails) {
        return new ApiResponse<>(false, message, "VALIDATION_ERROR", validationDetails);
    }
    
    /**
     * Create an authentication error response
     */
    public static <T> ApiResponse<T> authenticationError(String message) {
        return new ApiResponse<>(false, message, "AUTHENTICATION_ERROR", null);
    }
    
    /**
     * Create an authorization error response
     */
    public static <T> ApiResponse<T> authorizationError(String message) {
        return new ApiResponse<>(false, message, "AUTHORIZATION_ERROR", null);
    }
    
    /**
     * Create a not found error response
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(false, message, "NOT_FOUND", null);
    }
    
    /**
     * Create an internal server error response
     */
    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>(false, message, "INTERNAL_ERROR", null);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return String.format("ApiResponse{success=%s, message='%s', errorCode='%s', timestamp=%s}", 
                           success, message, errorCode, timestamp);
    }
}