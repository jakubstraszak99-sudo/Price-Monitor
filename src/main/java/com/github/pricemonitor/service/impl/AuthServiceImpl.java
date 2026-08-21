package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.event.UserRegisterEvent;
import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.model.entity.User;
import com.github.pricemonitor.repository.UserRepository;
import com.github.pricemonitor.request.UserRegisterRequest;
import com.github.pricemonitor.security.TokenProvider;
import com.github.pricemonitor.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.pricemonitor.exception.ExceptionCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOPIC = "user-registration-topic";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final KafkaTemplate<String, UserRegisterEvent> kafkaTemplate;

    @Override
    @Transactional
    public void registerUser(final UserRegisterRequest request) {
        if (this.userRepository.findByEmail(request.email()).isPresent()) {
            throw new PmRuntimeException(E005);
        }

        this.userRepository.save(User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(this.passwordEncoder.encode(request.password()))
                .build());

        final UserRegisterEvent event = new UserRegisterEvent(request.email(), this.tokenProvider.generateVerificationToken(request.email()));
        log.debug("Sending user registration event to Kafka: topic={}, email={}", TOPIC, request.email());

        this.kafkaTemplate.send(TOPIC, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error(
                                "Failed to send user registration event to Kafka: topic={}, email={}",
                                TOPIC,
                                request.email(),
                                exception
                        );
                    } else {
                        log.debug(
                                "User registration event sent to Kafka: topic={}, partition={}, offset={}",
                                TOPIC,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
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
