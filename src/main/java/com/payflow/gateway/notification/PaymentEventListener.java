package com.payflow.gateway.notification;

/**
 * Observer interface for payment event notifications
 * Implements the Observer pattern for event handling
 */
public interface PaymentEventListener {
    
    /**
     * Called when a payment event occurs
     * @param event The payment event that occurred
     */
    void onPaymentEvent(PaymentEvent event);
    
    /**
     * Returns the types of events this listener is interested in
     * Return null or empty array to listen to all events
     */
    String[] getSupportedEventTypes();
    
    /**
     * Returns the listener's name/identifier for logging
     */
    String getListenerName();
    
    /**
     * Returns the priority of this listener (higher number = higher priority)
     * Higher priority listeners are notified first
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Whether this listener should be notified asynchronously
     */
    default boolean isAsynchronous() {
        return true;
    }
    
    /**
     * Called when an error occurs during event processing
     * Allows the listener to handle or log errors gracefully
     */
    default void onError(PaymentEvent event, Exception error) {
        // Default: do nothing, let the publisher handle it
    }
}