package com.github.pricemonitor.kafka;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.kafka.message.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import static com.github.pricemonitor.exception.ExceptionCode.E013;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, KafkaMessage> template;
    private final ReplyingKafkaTemplate<String, KafkaMessage, KafkaMessage> replyingTemplate;

    public <T extends KafkaMessage> void publish(final String topic, final String identifier, final T event) {
        this.send(new ProducerRecord<>(topic, identifier, event));
    }

    public <T extends KafkaMessage> void publish(final String topic, final String identifier, final T event, final String replyTopic) {
        final ProducerRecord<String, KafkaMessage> record = new ProducerRecord<>(topic, identifier, event);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes(StandardCharsets.UTF_8)));
        this.send(record);
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

    private void send(final ProducerRecord<String, KafkaMessage> record) {
        final String topic = record.topic();
        final String identifier = record.key();

        this.template.send(record)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to send event to Kafka: topic={}, identifier={}", topic, identifier, exception);
                    } else {
                        final RecordMetadata metadata = result.getRecordMetadata();
                        log.debug("Event sent to Kafka: topic={}, identifier={}, partition={}, offset={}",
                                topic, identifier, metadata.partition(), metadata.offset());
                    }
                });
    }

}