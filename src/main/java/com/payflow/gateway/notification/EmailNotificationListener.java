package com.payflow.gateway.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Email notification listener for sending payment notifications via email
 * Implements the Observer pattern for email delivery
 */
@Component
public class EmailNotificationListener implements PaymentEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationListener.class);
    
    @Override
    public void onPaymentEvent(PaymentEvent event) {
        logger.info("Processing email notification for event: {}", event.getEventId());
        
        String recipientEmail = getRecipientEmail(event);
        
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            logger.debug("No email recipient configured for merchant: {}", event.getMerchantId());
            return;
        }
        
        try {
            sendEmail(recipientEmail, event);
            logger.info("Email notification sent successfully for event: {} to: {}", 
                       event.getEventId(), recipientEmail);
        } catch (Exception e) {
            logger.error("Failed to send email notification for event: {} to: {}", 
                        event.getEventId(), recipientEmail, e);
            throw e;
        }
    }
    
    @Override
    public String[] getSupportedEventTypes() {
        return new String[]{
            "payment.failed",
            "chargeback.received",
            "refund.processed"
        };
    }
    
    @Override
    public String getListenerName() {
        return "EmailNotificationListener";
    }
    
    @Override
    public int getPriority() {
        return 5; // Medium priority for email notifications
    }
    
    @Override
    public boolean isAsynchronous() {
        return true; // Emails should be sent asynchronously
    }
    
    @Override
    public void onError(PaymentEvent event, Exception error) {
        logger.error("Email notification failed for event: {}, will retry later", event.getEventId());
        // In a real implementation, you would queue this for retry
    }
    
    private void sendEmail(String recipientEmail, PaymentEvent event) {
        // In a real implementation, this would integrate with an email service
        // like SendGrid, AWS SES, or SMTP server
        
        String subject = createEmailSubject(event);
        String body = createEmailBody(event);
        
        logger.info("Sending email notification:");
        logger.info("To: {}", recipientEmail);
        logger.info("Subject: {}", subject);
        logger.info("Body: {}", body);
        
        // Simulate email sending
        try {
            Thread.sleep(100); // Simulate email service delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Mock: randomly fail 5% of emails to simulate real-world failures
        if (Math.random() < 0.05) {
            throw new EmailDeliveryException("Simulated email delivery failure");
        }
    }
    
    private String createEmailSubject(PaymentEvent event) {
        return switch (event.getEventType()) {
            case "payment.failed" -> "Payment Failed - Transaction " + event.getTransactionId();
            case "chargeback.received" -> "Chargeback Alert - Transaction " + event.getTransactionId();
            case "refund.processed" -> "Refund Processed - Transaction " + event.getTransactionId();
            default -> "Payment Notification - Transaction " + event.getTransactionId();
        };
    }
    
    private String createEmailBody(PaymentEvent event) {
        StringBuilder body = new StringBuilder();
        body.append("Dear Merchant,\n\n");
        
        switch (event.getEventType()) {
            case "payment.failed" -> {
                body.append("We wanted to inform you that a payment has failed.\n\n");
                body.append("Transaction ID: ").append(event.getTransactionId()).append("\n");
                body.append("Status: ").append(event.getStatus().getDescription()).append("\n");
                String errorMessage = event.getEventDataAsString("error_message");
                if (errorMessage != null) {
                    body.append("Reason: ").append(errorMessage).append("\n");
                }
            }
            case "chargeback.received" -> {
                body.append("A chargeback has been received for one of your transactions.\n\n");
                body.append("Transaction ID: ").append(event.getTransactionId()).append("\n");
                body.append("Status: ").append(event.getStatus().getDescription()).append("\n");
                body.append("Please review this transaction and provide any necessary documentation.\n");
            }
            case "refund.processed" -> {
                body.append("A refund has been successfully processed.\n\n");
                body.append("Transaction ID: ").append(event.getTransactionId()).append("\n");
                body.append("Status: ").append(event.getStatus().getDescription()).append("\n");
                String amount = event.getEventDataAsString("refund_amount");
                if (amount != null) {
                    body.append("Refund Amount: ").append(amount).append("\n");
                }
            }
            default -> {
                body.append("This is a notification regarding your transaction.\n\n");
                body.append("Transaction ID: ").append(event.getTransactionId()).append("\n");
                body.append("Status: ").append(event.getStatus().getDescription()).append("\n");
            }
        }
        
        body.append("\nEvent ID: ").append(event.getEventId()).append("\n");
        body.append("Timestamp: ").append(event.getTimestamp()).append("\n");
        body.append("\nBest regards,\n");
        body.append("PayFlow Team\n");
        
        return body.toString();
    }
    
    private String getRecipientEmail(PaymentEvent event) {
        // In a real implementation, this would look up the merchant's notification email
        // from the database or configuration
        String email = event.getEventDataAsString("notification_email");
        
        if (email == null) {
            // Mock email for testing
            email = "merchant-" + event.getMerchantId() + "@example.com";
        }
        
        return email;
    }
    
    /**
     * Exception thrown when email delivery fails
     */
    public static class EmailDeliveryException extends RuntimeException {
        public EmailDeliveryException(String message) {
            super(message);
        }
        
        public EmailDeliveryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}