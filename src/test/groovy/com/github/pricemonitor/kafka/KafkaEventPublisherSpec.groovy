package com.github.pricemonitor.kafka

import com.github.pricemonitor.exception.ExceptionCode
import com.github.pricemonitor.exception.PmRuntimeException
import com.github.pricemonitor.kafka.message.KafkaMessage
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.requestreply.RequestReplyFuture
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.SendResult
import spock.lang.Specification
import spock.lang.Subject

import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class KafkaEventPublisherSpec extends Specification {

    def template = Mock(KafkaTemplate)
    def replyingTemplate = Mock(ReplyingKafkaTemplate)

    @Subject
    def publisher = new KafkaEventPublisher(this.template, this.replyingTemplate)

    def topic = "test-topic"
    def identifier = "test-id"
    def event = Mock(KafkaMessage)

    def "Should publish event properly"() {
        given:
            def sendResult = new SendResult<>(
                    new ProducerRecord<>(this.topic, this.identifier, this.event),
                    new RecordMetadata(new TopicPartition(this.topic, 0), 0L, 0, 0, 0, 0)
            )
            def future = CompletableFuture.completedFuture(sendResult)

        when:
            this.publisher.publish(this.topic, this.identifier, this.event)

        then:
            1 * this.template.send({ ProducerRecord record ->
                record.topic() == this.topic && record.key() == this.identifier && record.value() == this.event
                        && record.headers().lastHeader(KafkaHeaders.REPLY_TOPIC) == null
            }) >> future
    }

    def "Should publish event with reply topic header"() {
        given:
            def replyTopic = "test-reply-topic"
            def sendResult = new SendResult<>(
                    new ProducerRecord<>(this.topic, this.identifier, this.event),
                    new RecordMetadata(new TopicPartition(this.topic, 0), 0L, 0, 0, 0, 0)
            )
            def future = CompletableFuture.completedFuture(sendResult)

        when:
            this.publisher.publish(this.topic, this.identifier, this.event, replyTopic)

        then:
            1 * this.template.send({ ProducerRecord record ->
                record.topic() == this.topic && record.key() == this.identifier && record.value() == this.event
                        && record.headers().lastHeader(KafkaHeaders.REPLY_TOPIC).value() == replyTopic.getBytes(StandardCharsets.UTF_8)
            }) >> future
    }

    def "Should publish and receive reply properly"() {
        given:
            def future = Mock(RequestReplyFuture)
            def replyEvent = Mock(KafkaMessage)
            def consumerRecord = new ConsumerRecord<>(this.topic, 0, 0L, this.identifier, replyEvent)

        when:
            def result = this.publisher.publishAndReceive(this.topic, this.identifier, this.event)

        then:
            1 * this.replyingTemplate.sendAndReceive({ ProducerRecord record ->
                record.topic() == this.topic && record.key() == this.identifier && record.value() == this.event
            }) >> future
            1 * future.get() >> consumerRecord
            result == replyEvent
    }

    def "Should throw exception when receiving reply fails"() {
        given:
            def future = Mock(RequestReplyFuture)

        when:
            this.publisher.publishAndReceive(this.topic, this.identifier, this.event)

        then:
            1 * this.replyingTemplate.sendAndReceive(_) >> future
            1 * future.get() >> { throw new ExecutionException("Kafka Timeout", new RuntimeException()) }
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E013
    }

}