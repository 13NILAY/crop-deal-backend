package com.cropdeal.notification.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

@Component
public class NotificationEventListener {
    private final NotificationService service;
    private final SubscriptionEmailClient subscriptionEmailClient;
    private final ObjectMapper objectMapper;
    public NotificationEventListener(NotificationService service,
            SubscriptionEmailClient subscriptionEmailClient, ObjectMapper objectMapper) {
        this.service = service;
        this.subscriptionEmailClient = subscriptionEmailClient;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "notification-service.order-events")
    public void onOrderEvent(Message message) { save(message, "ORDER_CREATED"); }

    @RabbitListener(queues = "notification-service.payment-events")
    public void onPaymentEvent(Message message) { save(message, "PAYMENT_STATUS_CHANGED"); }

    @RabbitListener(queues = "notification-service.crop-events", containerFactory = "rawRabbitListenerContainerFactory")
    public void onCropEvent(Message message) {
        try {
            CropEvent event = objectMapper.readValue(message.getBody(), CropEvent.class);
            String text = String.format(
                    "A farmer %s a crop on CropDeal.%n%nCrop: %s%nType: %s%nQuantity: %s%nPrice: %s%nLocation: %s%nFarmer: %s%n%nOpen CropDeal to place an order.",
                    event.action().equalsIgnoreCase("UPDATED") ? "updated" : "listed",
                    event.cropName(), event.cropType(), event.quantity(), event.price(), event.location(), event.farmerEmail());
            for (String email : subscriptionEmailClient.findForFarmer(event.farmerId())) {
                service.save(email, "CROP_" + event.action(), text + "\nCrop ID: " + event.cropId());
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid crop notification event", ex);
        }
    }

    private void save(Message message, String type) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        service.save(recipient(payload), type, payload);
    }

    private String recipient(String message) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}").matcher(message);
        return matcher.find() ? matcher.group() : "system";
    }

    private record CropEvent(Long cropId, Long farmerId, String farmerEmail, String cropName, String cropType,
            Double quantity, Double price, String location, String action) { }
}