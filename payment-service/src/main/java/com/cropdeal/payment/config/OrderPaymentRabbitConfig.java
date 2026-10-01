package com.cropdeal.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderPaymentRabbitConfig {
    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    TopicExchange orderExchange() {
        return new TopicExchange("cropdeal.orders");
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange("cropdeal.dlx");
    }

    @Bean
    Queue paymentOrderQueue() {
        return QueueBuilder.durable("payment-service.order-events")
                .deadLetterExchange("cropdeal.dlx")
                .deadLetterRoutingKey("payment.order.failed")
                .build();
    }

    @Bean
    Queue paymentOrderDeadLetterQueue() {
        return QueueBuilder.durable("payment-service.order-events.dlq").build();
    }

    @Bean
    Binding paymentOrderDeadLetterBinding(Queue paymentOrderDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(paymentOrderDeadLetterQueue).to(deadLetterExchange)
                .with("payment.order.failed");
    }

    @Bean
    Binding orderCreatedBinding(Queue paymentOrderQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(paymentOrderQueue).to(orderExchange).with("order.created");
    }
}