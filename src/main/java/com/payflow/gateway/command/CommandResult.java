package com.payflow.gateway.command;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Result of command execution
 * Contains outcome, data, and metadata about command execution
 */
public class CommandResult {
    
    private final boolean success;
    private final String message;
    private final String errorCode;
    private final Object resultData;
    private final Map<String, Object> metadata;
    private final LocalDateTime timestamp;
    private final String commandId;
    private final Exception exception;
    
    private CommandResult(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.errorCode = builder.errorCode;
        this.resultData = builder.resultData;
        this.metadata = builder.metadata;
        this.timestamp = LocalDateTime.now();
        this.commandId = builder.commandId;
        this.exception = builder.exception;
    }
    
    // Factory methods for common results
    public static CommandResult success(String commandId, String message) {
        return new Builder(true, commandId)
                .message(message)
                .build();
    }
    
    public static CommandResult success(String commandId, String message, Object resultData) {
        return new Builder(true, commandId)
                .message(message)
                .resultData(resultData)
                .build();
    }
    
    public static CommandResult failure(String commandId, String errorCode, String message) {
        return new Builder(false, commandId)
                .errorCode(errorCode)
                .message(message)
                .build();
    }
    
    public static CommandResult failure(String commandId, String errorCode, String message, Exception exception) {
        return new Builder(false, commandId)
                .errorCode(errorCode)
                .message(message)
                .exception(exception)
                .build();
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object getResultData() {
        return resultData;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getResultData(Class<T> type) {
        if (resultData != null && type.isInstance(resultData)) {
            return (T) resultData;
        }
        return null;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getCommandId() {
        return commandId;
    }
    
    public Exception getException() {
        return exception;
    }
    
    public boolean hasException() {
        return exception != null;
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("CommandResult[SUCCESS: %s - %s]", commandId, message);
        } else {
            return String.format("CommandResult[FAILURE: %s - %s (%s)]", commandId, message, errorCode);
        }
    }
    
    /**
     * Builder for CommandResult
     */
    public static class Builder {
        private final boolean success;
        private final String commandId;
        private String message;
        private String errorCode;
        private Object resultData;
        private Map<String, Object> metadata;
        private Exception exception;
        
        public Builder(boolean success, String commandId) {
            this.success = success;
            this.commandId = commandId;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }
        
        public Builder resultData(Object resultData) {
            this.resultData = resultData;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder exception(Exception exception) {
            this.exception = exception;
            return this;
        }
        
        public CommandResult build() {
            return new CommandResult(this);
        }
    }
}