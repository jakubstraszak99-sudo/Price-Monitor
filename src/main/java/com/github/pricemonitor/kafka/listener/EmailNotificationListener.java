package com.github.pricemonitor.kafka.listener;

import com.github.pricemonitor.kafka.event.EmailNotificationEvent;
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
    public void handleRegistrationEvent(final EmailNotificationEvent event) {
        log.info("Received user registration event for email: {}", event.email());
        this.emailService.sendVerificationEmail(event.email(), event.token());
    }

    @KafkaListener(topics = PASSWORD_RESET_TOPIC, containerFactory = "emailKafkaListenerContainerFactory")
    public void handleResetPasswordEvent(final EmailNotificationEvent event) {
        log.info("Received password reset event for email: {}", event.email());
        this.emailService.sendPasswordResetEmail(event.email(), event.token());
    }

}
