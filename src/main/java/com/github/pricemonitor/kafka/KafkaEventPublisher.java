package com.github.pricemonitor.kafka;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.kafka.message.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

import static com.github.pricemonitor.exception.ExceptionCode.E013;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, KafkaMessage> template;
    private final ReplyingKafkaTemplate<String, KafkaMessage, KafkaMessage> replyingTemplate;

    public <T extends KafkaMessage> void publish(final String topic, final String identifier, final T event) {
        this.template.send(topic, identifier, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to send event to Kafka: topic={}, identifier={}", topic, identifier, exception);
                    } else {
                        log.debug("Event sent to Kafka: topic={}, identifier={}, partition={}, offset={}",
                                topic, identifier, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public <T extends KafkaMessage, R extends KafkaMessage> R publishAndReceive(final String topic, final String identifier, final T event) {
        log.debug("Sending request event and waiting for reply: topic={}, identifier={}", topic, identifier);

        final ProducerRecord<String, KafkaMessage> record = new ProducerRecord<>(topic, identifier, event);
        final RequestReplyFuture<String, KafkaMessage, KafkaMessage> replyFuture = this.replyingTemplate.sendAndReceive(record);

        try {
            final ConsumerRecord<String, KafkaMessage> consumerRecord = replyFuture.get();
            return (R) consumerRecord.value();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to receive reply for event: topic={}, identifier={}", topic, identifier, e);
            throw new PmRuntimeException(E013);
        }
    }

}
