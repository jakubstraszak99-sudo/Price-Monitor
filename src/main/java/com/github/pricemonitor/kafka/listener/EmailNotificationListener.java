package com.github.pricemonitor.kafka.listener;

import com.github.pricemonitor.kafka.message.EmailNotificationMessage;
import com.github.pricemonitor.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.github.pricemonitor.utils.KafkaUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationListener {

    private final EmailService emailService;

    @KafkaListener(topics = REGISTRATION_TOPIC, containerFactory = "emailKafkaListenerContainerFactory")
    public void handleRegistrationEvent(final EmailNotificationMessage message) {
        log.info("Received user registration event for email: {}", message.email());
        this.emailService.sendVerificationEmail(message.email(), message.token());
    }

    @KafkaListener(topics = PASSWORD_RESET_TOPIC, containerFactory = "emailKafkaListenerContainerFactory")
    public void handleResetPasswordEvent(final EmailNotificationMessage message) {
        log.info("Received password reset event for email: {}", message.email());
        this.emailService.sendPasswordResetEmail(message.email(), message.token());
    }

}
