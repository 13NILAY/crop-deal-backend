package com.cropdeal.order.service;

import com.cropdeal.order.config.RabbitConfig;
import com.cropdeal.order.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OrderCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.ORDER_EXCHANGE, "order.created", event);
        } catch (AmqpException ex) {
            log.warn("Order {} saved but OrderCreatedEvent was not published: {}", event.orderId(), ex.getMessage());
        }
    }
}