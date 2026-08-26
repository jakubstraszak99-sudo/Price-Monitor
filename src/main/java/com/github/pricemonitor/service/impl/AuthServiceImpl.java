package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.kafka.KafkaEventPublisher;
import com.github.pricemonitor.kafka.message.EmailNotificationMessage;
import com.github.pricemonitor.model.dto.AccessTokenExpiryInfo;
import com.github.pricemonitor.model.dto.AuthTokenSet;
import com.github.pricemonitor.model.entity.UserEntity;
import com.github.pricemonitor.redis.RefreshToken;
import com.github.pricemonitor.redis.RefreshTokenRedisRepository;
import com.github.pricemonitor.repository.UserRepository;
import com.github.pricemonitor.security.TokenProvider;
import com.github.pricemonitor.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.github.pricemonitor.exception.ExceptionCode.*;
import static com.github.pricemonitor.utils.KafkaUtil.REGISTRATION_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final KafkaEventPublisher eventPublisher;
    private final RefreshTokenRedisRepository redisRepository;

    @Override
    @Transactional
    public void registerUser(final String username, final String email, final String password) {
        this.userRepository.findByEmail(email).ifPresent(
                existingUser -> {
                    if (existingUser.getVerified()) {
                        throw new PmRuntimeException(E007);
                    }

                    this.userRepository.delete(existingUser);
                    this.userRepository.flush();
                });

        final UserEntity user = UserEntity.builder()
                .username(username)
                .email(email)
                .passwordHash(this.passwordEncoder.encode(password))
                .build();

        this.userRepository.save(user);

        final EmailNotificationMessage message = new EmailNotificationMessage(email, this.tokenProvider.generateVerificationToken(user.getPublicId()));
        this.eventPublisher.publish(REGISTRATION_TOPIC, email, message);
    }

    @Override
    @Transactional
    public void verifyAccount(final String token) {
        final UUID publicId = this.tokenProvider.extractUserPublicId(token);
        final UserEntity user = this.userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PmRuntimeException(E001));

        if (user.getVerified()) {
            throw new PmRuntimeException(E002);
        }

        user.setVerified(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthTokenSet login(final String login, final String password) {
        final UserEntity user = this.userRepository
                .findByUsernameOrEmail(login, login)
                .orElseThrow(() -> new PmRuntimeException(E008));

        if (!this.passwordEncoder.matches(password, user.getPasswordHash()) || !user.getVerified()) {
            throw new PmRuntimeException(E008);
        }

        final String accessToken = this.tokenProvider.generateAccessToken(user.getPublicId());
        final RefreshToken refreshToken = this.tokenProvider.generateRefreshToken(user.getPublicId());

        this.redisRepository.save(refreshToken);

        return new AuthTokenSet(accessToken, refreshToken.getTokenId(), this.tokenProvider.getAccessExpirationInSeconds(), refreshToken.getExpirationInSeconds());
    }

    @Override
    public void logout(final String refreshTokenValue) {
        if (refreshTokenValue != null) {
            this.redisRepository.deleteById(refreshTokenValue);
        }
    }

    @Override
    public AccessTokenExpiryInfo refreshToken(final String refreshTokenValue) {
        final RefreshToken refreshToken = this.redisRepository.findById(refreshTokenValue)
                .orElseThrow(() -> new PmRuntimeException(E009));
        final String accessToken = this.tokenProvider.generateAccessToken(refreshToken.getUserPublicId());
        final long accessExpirationSeconds = this.tokenProvider.getAccessExpirationInSeconds();

        return new AccessTokenExpiryInfo(accessToken, accessExpirationSeconds);
    }

}
