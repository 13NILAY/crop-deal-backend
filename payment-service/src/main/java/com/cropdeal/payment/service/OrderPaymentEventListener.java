package com.cropdeal.payment.service;

import com.cropdeal.payment.dto.CreatePaymentRequest;
import com.cropdeal.payment.event.OrderCreatedEvent;
import com.cropdeal.payment.repository.PaymentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderPaymentEventListener.class);
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public OrderPaymentEventListener(PaymentService paymentService, PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    @RabbitListener(queues = "payment-service.order-events")
    public void onOrderCreated(OrderCreatedEvent event) {
        if (paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(event.orderId()).isPresent()) return;
        try {
            paymentService.create(event.dealerEmail(), new CreatePaymentRequest(event.orderId(), event.totalPrice()));
            log.info("Created Razorpay payment order for CropDeal order {}", event.orderId());
        } catch (RuntimeException ex) {
            log.error("Could not create Razorpay payment order for CropDeal order {}: {}", event.orderId(), ex.getMessage(), ex);
            throw ex;
        }
    }
}