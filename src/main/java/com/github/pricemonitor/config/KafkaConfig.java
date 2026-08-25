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

import static com.github.pricemonitor.utils.KafkaUtil.*;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

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
        Map<String, Object> props = baseConsumerProps(EMAIL_SENDER_GROUP);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, EmailNotificationMessage.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, KafkaMessage> scraperReplyConsumerFactory() {
        Map<String, Object> props = baseConsumerProps(SCRAPER_REPLY_GROUP);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, ScraperReplyMessage.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, KafkaMessage> scraperRequestConsumerFactory() {
        Map<String, Object> props = baseConsumerProps(SCRAPER_REQUEST_GROUP);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, ScraperRequestMessage.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> emailKafkaListenerContainerFactory(
            final ConsumerFactory<String, KafkaMessage> emailConsumerFactory) {
        final ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(emailConsumerFactory);
        return factory;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, KafkaMessage> replyContainer(
            final ConsumerFactory<String, KafkaMessage> scraperReplyConsumerFactory) {
        final ContainerProperties containerProperties = new ContainerProperties(SCRAPER_REPLY_TOPIC);
        return new ConcurrentMessageListenerContainer<>(scraperReplyConsumerFactory, containerProperties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> scraperRequestContainerFactory(
            final ConsumerFactory<String, KafkaMessage> scraperRequestConsumerFactory,
            final KafkaTemplate<String, KafkaMessage> kafkaTemplate) {
        final ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(scraperRequestConsumerFactory);
        factory.setReplyTemplate(kafkaTemplate);
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