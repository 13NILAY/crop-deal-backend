package com.cropdeal.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

@Configuration
public class RabbitConfig {
    public static final String CROP_EXCHANGE = "cropdeal.crops";

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    SimpleRabbitListenerContainerFactory rawRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        return factory;
    }

    @Bean TopicExchange orderExchange() { return new TopicExchange("cropdeal.orders"); }
    @Bean TopicExchange paymentExchange() { return new TopicExchange("cropdeal.payments"); }
    @Bean TopicExchange cropExchange() { return new TopicExchange(CROP_EXCHANGE); }
    @Bean DirectExchange deadLetterExchange() { return new DirectExchange("cropdeal.dlx"); }
    @Bean Queue notificationOrderQueue() {
        return QueueBuilder.durable("notification-service.order-events")
                .deadLetterExchange("cropdeal.dlx").deadLetterRoutingKey("notification.order.failed").build();
    }
    @Bean Queue notificationPaymentQueue() {
        return QueueBuilder.durable("notification-service.payment-events")
                .deadLetterExchange("cropdeal.dlx").deadLetterRoutingKey("notification.payment.failed").build();
    }
    @Bean Queue notificationOrderDeadLetterQueue() {
        return QueueBuilder.durable("notification-service.order-events.dlq").build();
    }
    @Bean Queue notificationPaymentDeadLetterQueue() {
        return QueueBuilder.durable("notification-service.payment-events.dlq").build();
    }
    @Bean Queue notificationCropQueue() {
        return QueueBuilder.durable("notification-service.crop-events")
                .deadLetterExchange("cropdeal.dlx").deadLetterRoutingKey("notification.crop.failed").build();
    }
    @Bean Queue notificationCropDeadLetterQueue() {
        return QueueBuilder.durable("notification-service.crop-events.dlq").build();
    }
    @Bean Binding notificationOrderDeadLetterBinding(Queue notificationOrderDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(notificationOrderDeadLetterQueue).to(deadLetterExchange).with("notification.order.failed");
    }
    @Bean Binding notificationPaymentDeadLetterBinding(Queue notificationPaymentDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(notificationPaymentDeadLetterQueue).to(deadLetterExchange).with("notification.payment.failed");
    }
    @Bean Binding notificationCropDeadLetterBinding(Queue notificationCropDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(notificationCropDeadLetterQueue).to(deadLetterExchange).with("notification.crop.failed");
    }
    @Bean Binding orderBinding(Queue notificationOrderQueue, TopicExchange orderExchange) { return BindingBuilder.bind(notificationOrderQueue).to(orderExchange).with("order.created"); }
    @Bean Binding paymentSuccessBinding(Queue notificationPaymentQueue, TopicExchange paymentExchange) { return BindingBuilder.bind(notificationPaymentQueue).to(paymentExchange).with("payment.*"); }
    @Bean Binding cropBinding(Queue notificationCropQueue, TopicExchange cropExchange) { return BindingBuilder.bind(notificationCropQueue).to(cropExchange).with("crop.listed"); }
}