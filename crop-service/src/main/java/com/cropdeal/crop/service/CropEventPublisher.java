package com.cropdeal.crop.service;

import com.cropdeal.crop.config.RabbitConfig;
import com.cropdeal.crop.event.CropListedEvent;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CropEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public CropEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(CropListedEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.CROP_EXCHANGE, "crop.listed", event);
        } catch (AmqpException ignored) {
            // A broker outage must not make a successfully saved crop fail.
        }
    }
}