package com.payflow.gateway.command;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base interface for payment commands implementing the Command pattern
 * Provides execute, undo, and audit capabilities for payment operations
 */
public interface PaymentCommand {
    
    /**
     * Executes the payment command
     * @return CommandResult containing operation outcome
     */
    CommandResult execute();
    
    /**
     * Undoes the payment command if possible
     * @return CommandResult containing undo outcome
     */
    CommandResult undo();
    
    /**
     * Checks if this command can be undone
     * @return true if the command supports undo operation
     */
    boolean canUndo();
    
    /**
     * Gets the unique identifier for this command
     * @return command ID
     */
    String getCommandId();
    
    /**
     * Gets the type of this command (CHARGE, REFUND, VOID, etc.)
     * @return command type
     */
    CommandType getCommandType();
    
    /**
     * Gets the transaction ID this command operates on
     * @return transaction ID
     */
    String getTransactionId();
    
    /**
     * Gets additional command parameters
     * @return map of command parameters
     */
    Map<String, Object> getParameters();
    
    /**
     * Gets the timestamp when command was created
     * @return creation timestamp
     */
    LocalDateTime getCreatedAt();
    
    /**
     * Gets the timestamp when command was executed
     * @return execution timestamp, null if not executed
     */
    LocalDateTime getExecutedAt();
    
    /**
     * Gets the current state of the command
     * @return command state
     */
    CommandState getState();
    
    /**
     * Gets the execution context/metadata
     * @return execution context
     */
    Map<String, Object> getExecutionContext();
}

/**
 * Enumeration for command types
 */
enum CommandType {
    CHARGE("charge", "Charge payment from customer"),
    REFUND("refund", "Refund payment to customer"),
    PARTIAL_REFUND("partial_refund", "Partial refund to customer"),
    VOID("void", "Void/cancel transaction"),
    CAPTURE("capture", "Capture authorized payment"),
    AUTHORIZE("authorize", "Authorize payment without capture"),
    DISPUTE("dispute", "Process dispute/chargeback"),
    SETTLE("settle", "Settle transaction");
    
    private final String code;
    private final String description;
    
    CommandType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}

/**
 * Enumeration for command states
 */
enum CommandState {
    CREATED("created", "Command created but not executed"),
    EXECUTING("executing", "Command is currently executing"),
    EXECUTED("executed", "Command executed successfully"),
    FAILED("failed", "Command execution failed"),
    UNDOING("undoing", "Command is being undone"),
    UNDONE("undone", "Command was successfully undone"),
    UNDO_FAILED("undo_failed", "Command undo operation failed");
    
    private final String code;
    private final String description;
    
    CommandState(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}