package com.payflow.gateway.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract base class for payment commands
 * Provides common functionality and template method pattern
 */
public abstract class AbstractPaymentCommand implements PaymentCommand {
    
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final String commandId;
    private final String transactionId;
    private final CommandType commandType;
    private final Map<String, Object> parameters;
    private final LocalDateTime createdAt;
    private final Map<String, Object> executionContext;
    
    private CommandState state;
    private LocalDateTime executedAt;
    private CommandResult lastResult;
    
    protected AbstractPaymentCommand(CommandType commandType, String transactionId, Map<String, Object> parameters) {
        this.commandId = generateCommandId();
        this.commandType = commandType;
        this.transactionId = transactionId;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.executionContext = new HashMap<>();
        this.state = CommandState.CREATED;
    }
    
    @Override
    public final CommandResult execute() {
        if (state != CommandState.CREATED && state != CommandState.FAILED) {
            return CommandResult.failure(commandId, "INVALID_STATE", 
                    "Command cannot be executed in state: " + state);
        }
        
        setState(CommandState.EXECUTING);
        logger.info("Executing command: {} for transaction: {}", commandId, transactionId);
        
        try {
            // Validate command before execution
            CommandResult validationResult = validateCommand();
            if (!validationResult.isSuccess()) {
                setState(CommandState.FAILED);
                return validationResult;
            }
            
            // Execute the specific command logic
            CommandResult result = doExecute();
            
            if (result.isSuccess()) {
                setState(CommandState.EXECUTED);
                this.executedAt = LocalDateTime.now();
                logger.info("Command executed successfully: {}", commandId);
            } else {
                setState(CommandState.FAILED);
                logger.warn("Command execution failed: {} - {}", commandId, result.getMessage());
            }
            
            this.lastResult = result;
            return result;
            
        } catch (Exception e) {
            setState(CommandState.FAILED);
            logger.error("Command execution error: {}", commandId, e);
            CommandResult errorResult = CommandResult.failure(commandId, "EXECUTION_ERROR", 
                    "Command execution failed: " + e.getMessage(), e);
            this.lastResult = errorResult;
            return errorResult;
        }
    }
    
    @Override
    public final CommandResult undo() {
        if (!canUndo()) {
            return CommandResult.failure(commandId, "CANNOT_UNDO", 
                    "Command cannot be undone in state: " + state);
        }
        
        setState(CommandState.UNDOING);
        logger.info("Undoing command: {} for transaction: {}", commandId, transactionId);
        
        try {
            CommandResult result = doUndo();
            
            if (result.isSuccess()) {
                setState(CommandState.UNDONE);
                logger.info("Command undone successfully: {}", commandId);
            } else {
                setState(CommandState.UNDO_FAILED);
                logger.warn("Command undo failed: {} - {}", commandId, result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            setState(CommandState.UNDO_FAILED);
            logger.error("Command undo error: {}", commandId, e);
            return CommandResult.failure(commandId, "UNDO_ERROR", 
                    "Command undo failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean canUndo() {
        return state == CommandState.EXECUTED && supportsUndo();
    }
    
    /**
     * Template method for command validation
     */
    protected CommandResult validateCommand() {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            return CommandResult.failure(commandId, "INVALID_TRANSACTION", "Transaction ID is required");
        }
        
        return doValidate();
    }
    
    /**
     * Abstract method for specific command execution logic
     */
    protected abstract CommandResult doExecute();
    
    /**
     * Abstract method for specific command undo logic
     */
    protected abstract CommandResult doUndo();
    
    /**
     * Abstract method for specific command validation
     */
    protected abstract CommandResult doValidate();
    
    /**
     * Whether this command type supports undo operation
     */
    protected abstract boolean supportsUndo();
    
    // Getters
    @Override
    public String getCommandId() {
        return commandId;
    }
    
    @Override
    public CommandType getCommandType() {
        return commandType;
    }
    
    @Override
    public String getTransactionId() {
        return transactionId;
    }
    
    @Override
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }
    
    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    
    @Override
    public CommandState getState() {
        return state;
    }
    
    @Override
    public Map<String, Object> getExecutionContext() {
        return new HashMap<>(executionContext);
    }
    
    public CommandResult getLastResult() {
        return lastResult;
    }
    
    // Protected utility methods
    protected void addExecutionContext(String key, Object value) {
        executionContext.put(key, value);
    }
    
    protected Object getParameter(String key) {
        return parameters.get(key);
    }
    
    protected String getParameterAsString(String key) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : null;
    }
    
    protected void setState(CommandState newState) {
        CommandState oldState = this.state;
        this.state = newState;
        addExecutionContext("state_transition", oldState + " -> " + newState);
    }
    
    private String generateCommandId() {
        return "cmd_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}