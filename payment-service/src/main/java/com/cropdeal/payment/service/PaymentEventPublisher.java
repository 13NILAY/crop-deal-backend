package com.cropdeal.payment.service;

import com.cropdeal.payment.config.RabbitConfig;
import com.cropdeal.payment.event.PaymentStatusChangedEvent;
import org.springframework.amqp.AmqpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(PaymentStatusChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.PAYMENT_EXCHANGE,
                    "payment." + event.status().name().toLowerCase(), event);
        } catch (AmqpException ex) {
            log.warn("Payment {} saved but event was not published: {}", event.paymentId(), ex.getMessage());
        }
    }
}