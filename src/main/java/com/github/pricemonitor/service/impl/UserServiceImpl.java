package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.kafka.KafkaEventPublisher;
import com.github.pricemonitor.kafka.message.EmailNotificationMessage;
import com.github.pricemonitor.model.entity.UserEntity;
import com.github.pricemonitor.repository.UserRepository;
import com.github.pricemonitor.security.TokenProvider;
import com.github.pricemonitor.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.github.pricemonitor.exception.ExceptionCode.*;
import static com.github.pricemonitor.utils.KafkaUtil.PASSWORD_RESET_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final KafkaEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUser(final UUID publicId) {
        return this.userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PmRuntimeException(E001));
    }

    @Override
    @Transactional
    public void updatePassword(final UUID userPublicId, final String oldPassword, final String newPassword) {
        final UserEntity user = this.getUser(userPublicId);

        if (!this.passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new PmRuntimeException(E003);
        }

        if (this.passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new PmRuntimeException(E004);
        }

        user.setPasswordHash(this.passwordEncoder.encode(newPassword));
        this.userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(final String email) {
        this.userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    final String resetToken = this.tokenProvider.generateVerificationToken(user.getPublicId());
                    final EmailNotificationMessage event = new EmailNotificationMessage(user.getEmail(), resetToken);
                    this.eventPublisher.publish(PASSWORD_RESET_TOPIC, user.getEmail(), event);
                }, () -> log.warn("Password reset requested for non-existent email: {}", email)
        );
    }

    @Override
    @Transactional
    public void resetPassword(final String resetToken, final String newPassword) {
        final UUID userPublicId = this.tokenProvider.extractUserPublicId(resetToken);
        final UserEntity user = this.getUser(userPublicId);
        user.setPasswordHash(this.passwordEncoder.encode(newPassword));
        this.userRepository.save(user);
    }

}
