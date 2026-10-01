package com.cropdeal.order.service;

import com.cropdeal.order.entity.OrderStatus;
import com.cropdeal.order.event.PaymentStatusChangedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {
    private final OrderService orderService;
    public PaymentEventListener(OrderService orderService) { this.orderService = orderService; }

    @RabbitListener(queues = "order-service.payment-events")
    public void onPaymentStatusChanged(PaymentStatusChangedEvent event) {
        OrderStatus status = switch (event.status()) {
            case "SUCCESS" -> OrderStatus.CONFIRMED;
            case "FAILED" -> OrderStatus.CANCELLED;
            default -> OrderStatus.PENDING_PAYMENT;
        };
        orderService.updateStatus(event.orderId(), status);
    }
}