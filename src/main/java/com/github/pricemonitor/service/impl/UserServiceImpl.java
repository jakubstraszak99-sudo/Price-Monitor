package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.kafka.KafkaEventPublisher;
import com.github.pricemonitor.kafka.event.EmailNotificationEvent;
import com.github.pricemonitor.repository.UserRepository;
import com.github.pricemonitor.security.TokenProvider;
import com.github.pricemonitor.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.pricemonitor.kafka.KafkaTopic.PASSWORD_RESET_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final KafkaEventPublisher eventPublisher;

    @Override
    @Transactional
    public void resetPassword(final String email) {
        this.userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    final String resetToken = this.tokenProvider.generateVerificationToken(user.getEmail());
                    final EmailNotificationEvent event = new EmailNotificationEvent(user.getEmail(), resetToken);
                    this.eventPublisher.publishEvent(PASSWORD_RESET_TOPIC, user.getEmail(), event);
                }, () -> log.warn("Password reset requested for non-existent email: {}", email)
        );
    }

}
