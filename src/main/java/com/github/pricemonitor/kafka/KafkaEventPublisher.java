package com.github.pricemonitor.kafka;

import com.github.pricemonitor.kafka.event.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, KafkaEvent> template;

    public <T extends KafkaEvent> void publishEvent(final String topic, final String identifier, final T event) {
        this.template.send(topic, event)
                .whenComplete((result, exception) ->{
                    if (exception != null) {
                        log.error(
                                "Failed to send event to Kafka: topic={}, identifier={}",
                                topic,
                                identifier,
                                exception
                        );
                    } else {
                        log.debug(
                                "Event sent to Kafka: topic={}, , identifier={}, partition={}, offset={}",
                                topic,
                                identifier,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }
}
