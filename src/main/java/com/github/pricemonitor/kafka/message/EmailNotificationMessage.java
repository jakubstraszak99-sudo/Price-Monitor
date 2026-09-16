package com.github.pricemonitor.kafka.message;

public record EmailNotificationMessage(
        String email,
        String item
) implements KafkaMessage {}
