package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.kafka.KafkaEventPublisher;
import com.github.pricemonitor.kafka.event.EmailNotificationEvent;
import com.github.pricemonitor.model.entity.User;
import com.github.pricemonitor.repository.UserRepository;
import com.github.pricemonitor.request.ChangePasswordRequest;
import com.github.pricemonitor.request.ResetPasswordRequest;
import com.github.pricemonitor.security.TokenProvider;
import com.github.pricemonitor.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.pricemonitor.exception.ExceptionCode.E001;
import static com.github.pricemonitor.kafka.KafkaTopic.PASSWORD_RESET_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final KafkaEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void resetPassword(final ResetPasswordRequest request) {
        this.userRepository.findByEmail(request.email()).ifPresentOrElse(
                user -> {
                    final String resetToken = this.tokenProvider.generateVerificationToken(user.getPublicId());
                    final EmailNotificationEvent event = new EmailNotificationEvent(user.getEmail(), resetToken);
                    this.eventPublisher.publishEvent(PASSWORD_RESET_TOPIC, user.getEmail(), event);
                }, () -> log.warn("Password reset requested for non-existent email: {}", request.email())
        );
    }

    @Override
    @Transactional
    public void changePassword(final ChangePasswordRequest request) {
        final User user = this.userRepository.findByPublicId(this.tokenProvider.extractUserPublicId(request.accessToken()))
                .orElseThrow(() -> new PmRuntimeException(E001));

        user.setPasswordHash(this.passwordEncoder.encode(request.password()));
        this.userRepository.save(user);
    }

}
