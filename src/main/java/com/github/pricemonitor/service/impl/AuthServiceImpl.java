package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.kafka.KafkaEventPublisher;
import com.github.pricemonitor.kafka.event.EmailNotificationEvent;
import com.github.pricemonitor.model.entity.User;
import com.github.pricemonitor.repository.UserRepository;
import com.github.pricemonitor.request.UserRegisterRequest;
import com.github.pricemonitor.security.TokenProvider;
import com.github.pricemonitor.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.pricemonitor.exception.ExceptionCode.*;
import static com.github.pricemonitor.kafka.KafkaTopic.REGISTRATION_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final KafkaEventPublisher eventPublisher;

    @Override
    @Transactional
    public void registerUser(final UserRegisterRequest request) {
        this.userRepository.findByEmail(request.email()).ifPresent(
                existingUser -> {
                    if (existingUser.getVerified()) {
                        throw new PmRuntimeException(E005);
                    }

                    this.userRepository.delete(existingUser);
                    this.userRepository.flush();
                });

        this.userRepository.save(User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(this.passwordEncoder.encode(request.password()))
                .build());

        final EmailNotificationEvent event = new EmailNotificationEvent(request.email(), this.tokenProvider.generateVerificationToken(request.email()));
        this.eventPublisher.publishEvent(REGISTRATION_TOPIC, request.email(), event);
    }

    @Override
    @Transactional
    public void verifyAccount(final String token) {
        final String email = this.tokenProvider.extractEmail(token);
        final User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new PmRuntimeException(E001));

        if (user.getVerified()) {
            throw new PmRuntimeException(E002);
        }

        user.setVerified(true);
        userRepository.save(user);
    }

}
