package com.github.pricemonitor.kafka.message;

public record ScraperRequestMessage(
        String url
) implements KafkaMessage {}
