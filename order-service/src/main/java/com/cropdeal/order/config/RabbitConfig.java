package com.cropdeal.order.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String ORDER_EXCHANGE = "cropdeal.orders";
    public static final String PAYMENT_EXCHANGE = "cropdeal.payments";

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean TopicExchange paymentExchange() { return new TopicExchange(PAYMENT_EXCHANGE); }
    @Bean DirectExchange deadLetterExchange() { return new DirectExchange("cropdeal.dlx"); }
    @Bean Queue orderPaymentQueue() {
        return org.springframework.amqp.core.QueueBuilder.durable("order-service.payment-events")
                .deadLetterExchange("cropdeal.dlx").deadLetterRoutingKey("order.payment.failed").build();
    }
    @Bean Queue orderPaymentDeadLetterQueue() {
        return org.springframework.amqp.core.QueueBuilder.durable("order-service.payment-events.dlq").build();
    }
    @Bean Binding orderPaymentDeadLetterBinding(Queue orderPaymentDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(orderPaymentDeadLetterQueue).to(deadLetterExchange).with("order.payment.failed");
    }
    @Bean Binding paymentSuccessBinding(Queue orderPaymentQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(orderPaymentQueue).to(paymentExchange).with("payment.success");
    }
    @Bean Binding paymentFailedBinding(Queue orderPaymentQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(orderPaymentQueue).to(paymentExchange).with("payment.failed");
    }
}