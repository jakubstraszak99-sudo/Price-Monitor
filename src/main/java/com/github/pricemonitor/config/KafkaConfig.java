package com.github.pricemonitor.config;

import com.github.pricemonitor.kafka.event.EmailNotificationEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.github.pricemonitor.utils.KafkaUtil.EMAIL_SENDER_GROUP;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, EmailNotificationEvent> emailConsumerFactory() {
        final Map<String, Object> props = this.baseConsumerProps(EMAIL_SENDER_GROUP);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, EmailNotificationEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmailNotificationEvent> emailKafkaListenerContainerFactory(
            final ConsumerFactory<String, EmailNotificationEvent> consumerFactory) {
        final ConcurrentKafkaListenerContainerFactory<String, EmailNotificationEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    private Map<String, Object> baseConsumerProps(final String groupId) {
        final Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.github.pricemonitor.kafka.event");
        props.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return props;
    }

}
