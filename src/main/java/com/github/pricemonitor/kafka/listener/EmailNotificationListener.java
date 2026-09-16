package com.github.pricemonitor.kafka.listener;

import com.github.pricemonitor.kafka.message.EmailNotificationMessage;
import com.github.pricemonitor.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.github.pricemonitor.kafka.KafkaConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationListener {

    private final EmailNotificationService emailNotificationService;

    @KafkaListener(topics = REGISTRATION_TOPIC, containerFactory = "emailKafkaListenerContainerFactory")
    public void handleRegistrationEvent(final EmailNotificationMessage message) {
        log.debug("Received user registration event for email: {}", message.email());
        this.emailNotificationService.sendVerificationEmail(message.email(), message.item());
    }

    @KafkaListener(topics = PASSWORD_RESET_TOPIC, containerFactory = "emailKafkaListenerContainerFactory")
    public void handleResetPasswordEvent(final EmailNotificationMessage message) {
        log.debug("Received password reset event for email: {}", message.email());
        this.emailNotificationService.sendPasswordResetEmail(message.email(), message.item());
    }

    @KafkaListener(topics = ALERT_NOTIFICATION_TOPIC, containerFactory = "emailKafkaListenerContainerFactory")
    public void handleAlertNotificationEvent(final EmailNotificationMessage message) {
        log.debug("Received alert notification event for email: {}", message.email());
        this.emailNotificationService.sendAlertNotificationEmail(message.email(), message.item());
    }

}
