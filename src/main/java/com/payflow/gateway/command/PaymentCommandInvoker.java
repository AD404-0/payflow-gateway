package com.payflow.gateway.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Command invoker that manages execution and history of payment commands
 * Implements command pattern with undo capability and audit trail
 */
@Service
public class PaymentCommandInvoker {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentCommandInvoker.class);
    
    private final Map<String, PaymentCommand> commandHistory = new ConcurrentHashMap<>();
    private final List<String> executionOrder = new CopyOnWriteArrayList<>();
    private final Map<String, List<PaymentCommand>> transactionCommands = new ConcurrentHashMap<>();
    
    /**
     * Executes a payment command
     */
    public CommandResult executeCommand(PaymentCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        logger.info("Executing command: {} for transaction: {}", 
                   command.getCommandId(), command.getTransactionId());
        
        try {
            CommandResult result = command.execute();
            
            // Store in history regardless of success/failure
            commandHistory.put(command.getCommandId(), command);
            executionOrder.add(command.getCommandId());
            
            // Group by transaction ID
            transactionCommands.computeIfAbsent(command.getTransactionId(), k -> new ArrayList<>())
                              .add(command);
            
            logger.info("Command {} completed with result: {}", command.getCommandId(), result.isSuccess());
            return result;
            
        } catch (Exception e) {
            logger.error("Error executing command: {}", command.getCommandId(), e);
            throw new CommandExecutionException("Failed to execute command: " + command.getCommandId(), e);
        }
    }
    
    /**
     * Executes multiple commands in sequence
     */
    public List<CommandResult> executeCommands(List<PaymentCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<CommandResult> results = new ArrayList<>();
        List<PaymentCommand> executedCommands = new ArrayList<>();
        
        for (PaymentCommand command : commands) {
            try {
                CommandResult result = executeCommand(command);
                results.add(result);
                executedCommands.add(command);
                
                // If command fails and we're in strict mode, rollback previous commands
                if (!result.isSuccess() && shouldRollbackOnFailure(command)) {
                    logger.warn("Command failed, rolling back previous commands: {}", command.getCommandId());
                    rollbackCommands(executedCommands);
                    break;
                }
                
            } catch (Exception e) {
                logger.error("Error in batch execution at command: {}", command.getCommandId(), e);
                // Add failure result
                results.add(CommandResult.failure(command.getCommandId(), "BATCH_EXECUTION_ERROR", 
                           "Error in batch execution: " + e.getMessage(), e));
                break;
            }
        }
        
        return results;
    }
    
    /**
     * Undoes a specific command by ID
     */
    public CommandResult undoCommand(String commandId) {
        PaymentCommand command = commandHistory.get(commandId);
        
        if (command == null) {
            return CommandResult.failure(commandId, "COMMAND_NOT_FOUND", 
                    "Command not found in history: " + commandId);
        }
        
        if (!command.canUndo()) {
            return CommandResult.failure(commandId, "CANNOT_UNDO", 
                    "Command cannot be undone: " + commandId);
        }
        
        logger.info("Undoing command: {}", commandId);
        
        try {
            return command.undo();
        } catch (Exception e) {
            logger.error("Error undoing command: {}", commandId, e);
            return CommandResult.failure(commandId, "UNDO_ERROR", 
                    "Error undoing command: " + e.getMessage(), e);
        }
    }
    
    /**
     * Undoes all commands for a specific transaction in reverse order
     */
    public List<CommandResult> undoTransactionCommands(String transactionId) {
        List<PaymentCommand> commands = transactionCommands.get(transactionId);
        
        if (commands == null || commands.isEmpty()) {
            return Collections.emptyList();
        }
        
        logger.info("Undoing all commands for transaction: {} (count: {})", transactionId, commands.size());
        
        List<CommandResult> results = new ArrayList<>();
        
        // Undo in reverse order
        for (int i = commands.size() - 1; i >= 0; i--) {
            PaymentCommand command = commands.get(i);
            if (command.canUndo()) {
                CommandResult result = undoCommand(command.getCommandId());
                results.add(result);
            }
        }
        
        return results;
    }
    
    /**
     * Gets command by ID
     */
    public PaymentCommand getCommand(String commandId) {
        return commandHistory.get(commandId);
    }
    
    /**
     * Gets all commands for a transaction
     */
    public List<PaymentCommand> getTransactionCommands(String transactionId) {
        List<PaymentCommand> commands = transactionCommands.get(transactionId);
        return commands != null ? new ArrayList<>(commands) : Collections.emptyList();
    }
    
    /**
     * Gets command execution history
     */
    public List<PaymentCommand> getCommandHistory() {
        return executionOrder.stream()
                           .map(commandHistory::get)
                           .filter(Objects::nonNull)
                           .toList();
    }
    
    /**
     * Gets commands by state
     */
    public List<PaymentCommand> getCommandsByState(CommandState state) {
        return commandHistory.values().stream()
                           .filter(cmd -> cmd.getState() == state)
                           .toList();
    }
    
    /**
     * Gets statistics about command execution
     */
    public CommandStatistics getStatistics() {
        int total = commandHistory.size();
        int executed = (int) commandHistory.values().stream().filter(cmd -> cmd.getState() == CommandState.EXECUTED).count();
        int failed = (int) commandHistory.values().stream().filter(cmd -> cmd.getState() == CommandState.FAILED).count();
        int undone = (int) commandHistory.values().stream().filter(cmd -> cmd.getState() == CommandState.UNDONE).count();
        
        return new CommandStatistics(total, executed, failed, undone);
    }
    
    private void rollbackCommands(List<PaymentCommand> commands) {
        for (int i = commands.size() - 1; i >= 0; i--) {
            PaymentCommand command = commands.get(i);
            if (command.canUndo()) {
                try {
                    command.undo();
                    logger.info("Rolled back command: {}", command.getCommandId());
                } catch (Exception e) {
                    logger.error("Error rolling back command: {}", command.getCommandId(), e);
                }
            }
        }
    }
    
    private boolean shouldRollbackOnFailure(PaymentCommand command) {
        // For now, always rollback on failure
        // In a real system, this could be configurable per command type
        return true;
    }
    
    /**
     * Statistics about command execution
     */
    public static class CommandStatistics {
        private final int total;
        private final int executed;
        private final int failed;
        private final int undone;
        
        public CommandStatistics(int total, int executed, int failed, int undone) {
            this.total = total;
            this.executed = executed;
            this.failed = failed;
            this.undone = undone;
        }
        
        public int getTotal() { return total; }
        public int getExecuted() { return executed; }
        public int getFailed() { return failed; }
        public int getUndone() { return undone; }
        public double getSuccessRate() { return total > 0 ? (double) executed / total : 0.0; }
    }
    
    /**
     * Exception thrown during command execution
     */
    public static class CommandExecutionException extends RuntimeException {
        public CommandExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}