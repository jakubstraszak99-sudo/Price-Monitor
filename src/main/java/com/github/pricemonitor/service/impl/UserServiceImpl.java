package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.kafka.KafkaEventPublisher;
import com.github.pricemonitor.kafka.message.EmailNotificationMessage;
import com.github.pricemonitor.model.dto.User;
import com.github.pricemonitor.model.entity.UserEntity;
import com.github.pricemonitor.model.mapper.UserMapper;
import com.github.pricemonitor.redis.model.PasswordResetToken;
import com.github.pricemonitor.redis.repository.PasswordResetTokenRedisRepository;
import com.github.pricemonitor.repository.UserRepository;
import com.github.pricemonitor.security.TokenProvider;
import com.github.pricemonitor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.github.pricemonitor.exception.ExceptionCode.*;
import static com.github.pricemonitor.kafka.KafkaConstants.PASSWORD_RESET_TOPIC;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TokenProvider tokenProvider;
    private final KafkaEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRedisRepository passwordResetTokenRedisRepository;

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserEntity(final UUID publicId) {
        return this.fetchUser(publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(final UUID publicId) {
        return this.userMapper.map(this.fetchUser(publicId));
    }

    @Override
    @Transactional
    public User updateSettings(final UUID userPublicId, final boolean emailAlertsEnabled) {
        final UserEntity user = this.fetchUser(userPublicId);
        user.setEmailAlertsEnabled(emailAlertsEnabled);
        return this.userMapper.map(user);
    }

    @Override
    @Transactional
    public void updatePassword(final UUID userPublicId, final String oldPassword, final String newPassword) {
        final UserEntity user = this.fetchUser(userPublicId);

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
        this.userRepository.findByEmailAndVerifiedTrue(email).ifPresent(
                user -> {
                    final String resetToken = this.tokenProvider.generateVerificationToken(user.getPublicId());
                    this.passwordResetTokenRedisRepository.save(PasswordResetToken.builder()
                            .tokenId(resetToken)
                            .expirationInSeconds(this.tokenProvider.getVerificationExpirationInSeconds())
                            .build());

                    final EmailNotificationMessage event = new EmailNotificationMessage(user.getEmail(), resetToken);
                    this.eventPublisher.publish(PASSWORD_RESET_TOPIC, user.getEmail(), event);
                }
        );
    }


    @Override
    @Transactional
    public void resetPassword(final String resetToken, final String newPassword) {
        if (!this.passwordResetTokenRedisRepository.existsById(resetToken)) {
            throw new PmRuntimeException(E006);
        }

        final UUID userPublicId = this.tokenProvider.extractUserPublicId(resetToken);
        final UserEntity user = this.fetchUser(userPublicId);
        user.setPasswordHash(this.passwordEncoder.encode(newPassword));

        this.userRepository.save(user);
        this.passwordResetTokenRedisRepository.deleteById(resetToken);
    }

    private UserEntity fetchUser(final UUID publicId) {
        return this.userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PmRuntimeException(E001));
    }

}
