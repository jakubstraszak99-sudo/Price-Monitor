package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.kafka.KafkaEventPublisher;
import com.github.pricemonitor.kafka.event.EmailNotificationEvent;
import com.github.pricemonitor.model.entity.User;
import com.github.pricemonitor.redis.RefreshToken;
import com.github.pricemonitor.redis.RefreshTokenRedisRepository;
import com.github.pricemonitor.repository.UserRepository;
import com.github.pricemonitor.request.TokenRefreshRequest;
import com.github.pricemonitor.request.UserLoginRequest;
import com.github.pricemonitor.request.UserRegisterRequest;
import com.github.pricemonitor.response.AuthResponse;
import com.github.pricemonitor.security.TokenProvider;
import com.github.pricemonitor.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
    private final RefreshTokenRedisRepository redisRepository;

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

        final User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(this.passwordEncoder.encode(request.password()))
                .build();

        this.userRepository.save(user);

        final EmailNotificationEvent event = new EmailNotificationEvent(request.email(), this.tokenProvider.generateVerificationToken(user.getPublicId()));
        this.eventPublisher.publishEvent(REGISTRATION_TOPIC, request.email(), event);
    }

    @Override
    @Transactional
    public void verifyAccount(final String token) {
        final UUID publicId = this.tokenProvider.extractUserPublicId(token);
        final User user = this.userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PmRuntimeException(E001));

        if (user.getVerified()) {
            throw new PmRuntimeException(E002);
        }

        user.setVerified(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(final UserLoginRequest request) {
        final User user = this.userRepository
                .findByUsernameOrEmail(request.login(), request.login())
                .orElseThrow(() -> new PmRuntimeException(E006));

        if (!this.passwordEncoder.matches(request.password(), user.getPasswordHash()) || !user.getVerified()) {
            throw new PmRuntimeException(E006);
        }

        final String accessToken = this.tokenProvider.generateAccessToken(user.getPublicId());
        final RefreshToken refreshToken = this.tokenProvider.generateRefreshToken(user.getPublicId());

        this.redisRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Override
    public AuthResponse refreshToken(final TokenRefreshRequest request) {
        final RefreshToken refreshToken = this.redisRepository.findById(request.refreshToken())
                .orElseThrow(() -> new PmRuntimeException(E007));

        final String newAccessToken = this.tokenProvider.generateAccessToken(refreshToken.getUserPublicId());
        return new AuthResponse(newAccessToken, refreshToken.getToken());
    }

}
