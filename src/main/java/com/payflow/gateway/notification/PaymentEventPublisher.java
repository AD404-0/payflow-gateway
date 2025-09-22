package com.payflow.gateway.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Publisher for payment events implementing the Observer pattern
 * Manages listeners and publishes events to interested parties
 */
@Service
public class PaymentEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventPublisher.class);
    
    private final List<PaymentEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, List<PaymentEventListener>> eventTypeListeners = new HashMap<>();
    
    /**
     * Registers a payment event listener
     */
    public void addListener(PaymentEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        
        listeners.add(listener);
        
        // Cache listeners by event type for efficient lookup
        String[] supportedTypes = listener.getSupportedEventTypes();
        if (supportedTypes == null || supportedTypes.length == 0) {
            // Listener interested in all events
            eventTypeListeners.computeIfAbsent("*", k -> new ArrayList<>()).add(listener);
        } else {
            for (String eventType : supportedTypes) {
                eventTypeListeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
            }
        }
        
        // Sort listeners by priority
        listeners.sort((l1, l2) -> Integer.compare(l2.getPriority(), l1.getPriority()));
        
        logger.info("Registered payment event listener: {} for events: {}", 
                   listener.getListenerName(), 
                   supportedTypes != null ? Arrays.toString(supportedTypes) : "ALL");
    }
    
    /**
     * Removes a payment event listener
     */
    public void removeListener(PaymentEventListener listener) {
        if (listener == null) return;
        
        listeners.remove(listener);
        
        // Remove from event type cache
        eventTypeListeners.values().forEach(list -> list.remove(listener));
        
        logger.info("Removed payment event listener: {}", listener.getListenerName());
    }
    
    /**
     * Publishes a payment event to all interested listeners
     */
    public void publishEvent(PaymentEvent event) {
        if (event == null) {
            logger.warn("Attempted to publish null event");
            return;
        }
        
        logger.debug("Publishing payment event: {}", event);
        
        List<PaymentEventListener> interestedListeners = getInterestedListeners(event.getEventType());
        
        if (interestedListeners.isEmpty()) {
            logger.debug("No listeners registered for event type: {}", event.getEventType());
            return;
        }
        
        int syncCount = 0;
        int asyncCount = 0;
        
        for (PaymentEventListener listener : interestedListeners) {
            try {
                if (listener.isAsynchronous()) {
                    publishEventAsync(event, listener);
                    asyncCount++;
                } else {
                    publishEventSync(event, listener);
                    syncCount++;
                }
            } catch (Exception e) {
                logger.error("Error notifying listener {} for event {}", 
                           listener.getListenerName(), event.getEventId(), e);
                
                try {
                    listener.onError(event, e);
                } catch (Exception errorHandlingException) {
                    logger.error("Error in listener error handler for {}", 
                               listener.getListenerName(), errorHandlingException);
                }
            }
        }
        
        logger.debug("Published event {} to {} listeners (sync: {}, async: {})", 
                    event.getEventId(), interestedListeners.size(), syncCount, asyncCount);
    }
    
    /**
     * Publishes multiple events in batch
     */
    public void publishEvents(List<PaymentEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        logger.debug("Publishing batch of {} events", events.size());
        
        for (PaymentEvent event : events) {
            publishEvent(event);
        }
    }
    
    /**
     * Gets the count of registered listeners
     */
    public int getListenerCount() {
        return listeners.size();
    }
    
    /**
     * Gets the count of listeners for a specific event type
     */
    public int getListenerCount(String eventType) {
        return getInterestedListeners(eventType).size();
    }
    
    /**
     * Gets listener information for monitoring/debugging
     */
    public List<String> getListenerInfo() {
        return listeners.stream()
                .map(listener -> String.format("%s (priority: %d, async: %s, events: %s)",
                        listener.getListenerName(),
                        listener.getPriority(),
                        listener.isAsynchronous(),
                        listener.getSupportedEventTypes() != null ? 
                            Arrays.toString(listener.getSupportedEventTypes()) : "ALL"))
                .collect(Collectors.toList());
    }
    
    @Async
    protected void publishEventAsync(PaymentEvent event, PaymentEventListener listener) {
        try {
            logger.trace("Notifying async listener {} for event {}", 
                        listener.getListenerName(), event.getEventId());
            listener.onPaymentEvent(event);
        } catch (Exception e) {
            logger.error("Error in async listener {} for event {}", 
                        listener.getListenerName(), event.getEventId(), e);
            listener.onError(event, e);
        }
    }
    
    private void publishEventSync(PaymentEvent event, PaymentEventListener listener) {
        logger.trace("Notifying sync listener {} for event {}", 
                    listener.getListenerName(), event.getEventId());
        listener.onPaymentEvent(event);
    }
    
    private List<PaymentEventListener> getInterestedListeners(String eventType) {
        List<PaymentEventListener> interested = new ArrayList<>();
        
        // Add listeners for this specific event type
        List<PaymentEventListener> typeSpecific = eventTypeListeners.get(eventType);
        if (typeSpecific != null) {
            interested.addAll(typeSpecific);
        }
        
        // Add listeners interested in all events
        List<PaymentEventListener> allEvents = eventTypeListeners.get("*");
        if (allEvents != null) {
            interested.addAll(allEvents);
        }
        
        return interested;
    }
}