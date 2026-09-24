package com.github.pricemonitor.config;

import com.github.pricemonitor.kafka.message.EmailNotificationMessage;
import com.github.pricemonitor.kafka.message.KafkaMessage;
import com.github.pricemonitor.kafka.message.ScraperReplyMessage;
import com.github.pricemonitor.kafka.message.ScraperRequestMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.github.pricemonitor.kafka.KafkaConstants.*;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.listener.concurrency}")
    private Integer concurrency;

    @Bean
    @Primary
    public KafkaTemplate<String, KafkaMessage> kafkaTemplate(final ProducerFactory<String, KafkaMessage> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ReplyingKafkaTemplate<String, KafkaMessage, KafkaMessage> replyingKafkaTemplate(
            final ProducerFactory<String, KafkaMessage> producerFactory,
            final ConcurrentMessageListenerContainer<String, KafkaMessage> replyContainer) {
        final ReplyingKafkaTemplate<String, KafkaMessage, KafkaMessage> template = new ReplyingKafkaTemplate<>(producerFactory, replyContainer);
        template.setDefaultReplyTimeout(Duration.ofSeconds(15));
        return template;
    }

    @Bean
    public ConsumerFactory<String, KafkaMessage> emailConsumerFactory() {
        return consumerFactory(EMAIL_SENDER_GROUP, EmailNotificationMessage.class);
    }

    @Bean
    public ConsumerFactory<String, KafkaMessage> scraperReplyConsumerFactory() {
        return consumerFactory(SCRAPER_REPLY_GROUP, ScraperReplyMessage.class);
    }

    @Bean
    public ConsumerFactory<String, KafkaMessage> scraperRequestConsumerFactory() {
        return consumerFactory(SCRAPER_REQUEST_GROUP, ScraperRequestMessage.class);
    }

    @Bean
    public ConsumerFactory<String, KafkaMessage> scraperSyncReplyConsumerFactory() {
        return consumerFactory(SCRAPER_SYNC_REPLY_GROUP, ScraperReplyMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> emailKafkaListenerContainerFactory(
            final ConsumerFactory<String, KafkaMessage> emailConsumerFactory) {
        return containerFactory(emailConsumerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> scraperReplyContainerFactory(
            final ConsumerFactory<String, KafkaMessage> scraperReplyConsumerFactory) {
        return containerFactory(scraperReplyConsumerFactory);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, KafkaMessage> replyContainer(
            final ConsumerFactory<String, KafkaMessage> scraperSyncReplyConsumerFactory) {
        final ContainerProperties containerProperties = new ContainerProperties(SCRAPER_SYNC_REPLY_TOPIC);
        return new ConcurrentMessageListenerContainer<>(scraperSyncReplyConsumerFactory, containerProperties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> scraperRequestContainerFactory(
            final ConsumerFactory<String, KafkaMessage> scraperRequestConsumerFactory,
            final ProducerFactory<String, KafkaMessage> producerFactory) {
        final ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> factory = containerFactory(scraperRequestConsumerFactory);
        final KafkaTemplate<String, KafkaMessage> replyTemplate = new KafkaTemplate<>(producerFactory);
        replyTemplate.setDefaultTopic(SCRAPER_REPLY_TOPIC);
        factory.setReplyTemplate(replyTemplate);
        return factory;
    }

    private ConsumerFactory<String, KafkaMessage> consumerFactory(final String groupId, final Class<? extends KafkaMessage> defaultType) {
        final Map<String, Object> props = baseConsumerProps(groupId);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, defaultType);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    private ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> containerFactory(final ConsumerFactory<String, KafkaMessage> consumerFactory) {
        final ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(this.concurrency);
        return factory;
    }

    private Map<String, Object> baseConsumerProps(final String groupId) {
        final Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.github.pricemonitor.kafka.message");
        props.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return props;
    }

}