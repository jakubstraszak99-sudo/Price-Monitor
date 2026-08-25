package com.github.pricemonitor.kafka.message;

public record EmailNotificationMessage(
        String email,
        String token
) implements KafkaMessage {}
