package com.github.pricemonitor.service

import com.github.pricemonitor.exception.ExceptionCode
import com.github.pricemonitor.exception.PmRuntimeException
import com.github.pricemonitor.kafka.KafkaEventPublisher
import com.github.pricemonitor.model.entity.UserEntity
import com.github.pricemonitor.redis.RefreshToken
import com.github.pricemonitor.redis.RefreshTokenRedisRepository
import com.github.pricemonitor.repository.UserRepository
import com.github.pricemonitor.security.TokenProvider
import com.github.pricemonitor.service.impl.AuthServiceImpl
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification
import spock.lang.Subject

class AuthServiceSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def passwordEncoder = Mock(PasswordEncoder)
    def tokenProvider = Mock(TokenProvider)
    def eventPublisher = Mock(KafkaEventPublisher)
    def redisRepository = Mock(RefreshTokenRedisRepository)

    @Subject
    def service = new AuthServiceImpl(
            this.userRepository,
            this.passwordEncoder,
            this.tokenProvider,
            this.eventPublisher,
            this.redisRepository
    )

    def userId = UUID.randomUUID()
    def email = "test@example.com"
    def username = "testuser"
    def password = "secretPassword"
    def passwordHash = "encodedHash"
    def token = "test-token"

    def "Should throw exception when trying to register with already verified email"() {
        given:
            def existingUser = UserEntity.builder()
                    .email(this.email)
                    .verified(true)
                    .build()
            this.userRepository.findByEmail(this.email) >> Optional.of(existingUser)

        when:
            this.service.registerUser(this.username, this.email, this.password)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E007
            0 * this.userRepository.save(_)
    }

    def "Should delete existing unverified user, save new and publish event"() {
        given:
            def unverifiedUser = UserEntity.builder()
                    .email(this.email)
                    .verified(false)
                    .build()
            this.userRepository.findByEmail(this.email) >> Optional.of(unverifiedUser)
            this.passwordEncoder.encode(this.password) >> this.passwordHash
            this.tokenProvider.generateVerificationToken(_ as UUID) >> this.token

        when:
            this.service.registerUser(this.username, this.email, this.password)

        then:
            1 * this.userRepository.delete(unverifiedUser)
            1 * this.userRepository.flush()
            1 * this.userRepository.save({ it.email == this.email && it.passwordHash == this.passwordHash })
            1 * this.eventPublisher.publish(_, this.email, { event -> event.email() == this.email && event.token() == this.token })
    }

    def "Should register new user and publish event when email does not exist"() {
        given:
            this.userRepository.findByEmail(this.email) >> Optional.empty()
            this.passwordEncoder.encode(this.password) >> this.passwordHash
            this.tokenProvider.generateVerificationToken(_ as UUID) >> this.token

        when:
            this.service.registerUser(this.username, this.email, this.password)

        then:
            0 * this.userRepository.delete(_)
            1 * this.userRepository.save({ it.email == this.email && it.passwordHash == this.passwordHash })
            1 * this.eventPublisher.publish(_, this.email, { event -> event.email() == this.email && event.token() == this.token })
    }

    def "Should verify account properly"() {
        given:
            def user = UserEntity.builder()
                    .publicId(this.userId)
                    .verified(false)
                    .build()
            this.tokenProvider.extractUserPublicId(this.token) >> this.userId
            this.userRepository.findByPublicId(this.userId) >> Optional.of(user)

        when:
            this.service.verifyAccount(this.token)

        then:
            user.getVerified()
            1 * this.userRepository.save(user)
    }

    def "Should throw exception when verifying non-existent user"() {
        given:
            this.tokenProvider.extractUserPublicId(this.token) >> this.userId
            this.userRepository.findByPublicId(this.userId) >> Optional.empty()

        when:
            this.service.verifyAccount(this.token)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E001
            0 * this.userRepository.save(_)
    }

    def "Should throw exception when account is already verified"() {
        given:
            def user = UserEntity.builder()
                    .publicId(this.userId)
                    .verified(true)
                    .build()
            this.tokenProvider.extractUserPublicId(this.token) >> this.userId
            this.userRepository.findByPublicId(this.userId) >> Optional.of(user)

        when:
            this.service.verifyAccount(this.token)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E002
            0 * this.userRepository.save(_)
    }

    def "Should successfully login and return token set"() {
        given:
            def user = UserEntity.builder()
                    .publicId(this.userId)
                    .passwordHash(this.passwordHash)
                    .verified(true)
                    .build()
            this.userRepository.findByUsernameOrEmail(this.username, this.username) >> Optional.of(user)
            this.passwordEncoder.matches(this.password, this.passwordHash) >> true

            def accessToken = "access-token-123"
            def refreshToken = new RefreshToken(tokenId: "refresh-token-123", userPublicId: this.userId, expirationInSeconds: 3600L)

            this.tokenProvider.generateAccessToken(this.userId) >> accessToken
            this.tokenProvider.generateRefreshToken(this.userId) >> refreshToken
            this.tokenProvider.getAccessExpirationInSeconds() >> 900L

        when:
            def result = this.service.login(this.username, this.password)

        then:
            1 * this.redisRepository.save(refreshToken)
            result.accessToken() == accessToken
            result.refreshToken() == refreshToken.getTokenId()
            result.accessExpirationSeconds() == 900L
    }

    def "Should throw exception on login when user not found"() {
        given:
            this.userRepository.findByUsernameOrEmail(this.username, this.username) >> Optional.empty()

        when:
            this.service.login(this.username, this.password)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E008
            0 * this.redisRepository.save(_)
    }

    def "Should throw exception on login when password does not match"() {
        given:
            def wrongPassword = "wrongPassword"
            def user = UserEntity.builder()
                    .publicId(this.userId)
                    .passwordHash(this.passwordHash)
                    .verified(true)
                    .build()
            this.userRepository.findByUsernameOrEmail(this.username, this.username) >> Optional.of(user)
            this.passwordEncoder.matches(wrongPassword, this.passwordHash) >> false

        when:
            this.service.login(this.username, wrongPassword)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E008
            0 * this.redisRepository.save(_)
    }

    def "Should throw exception on login when account is not verified"() {
        given:
            def user = UserEntity.builder()
                    .publicId(this.userId)
                    .passwordHash(this.passwordHash)
                    .verified(false)
                    .build()
            this.userRepository.findByUsernameOrEmail(this.username, this.username) >> Optional.of(user)
            this.passwordEncoder.matches(this.password, this.passwordHash) >> true

        when:
            this.service.login(this.username, this.password)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E008
            0 * this.redisRepository.save(_)
    }

    def "Should generate new access token using valid refresh token"() {
        given:
            def validRefreshToken = new RefreshToken(tokenId: this.token, userPublicId: this.userId)
            this.redisRepository.findById(this.token) >> Optional.of(validRefreshToken)

            def newAccessToken = "new-access-token"
            this.tokenProvider.generateAccessToken(this.userId) >> newAccessToken
            this.tokenProvider.getAccessExpirationInSeconds() >> 900L

        when:
            def result = this.service.refreshToken(this.token)

        then:
            result.accessToken() == newAccessToken
            result.accessExpirationSeconds() == 900L
    }

    def "Should throw exception when refresh token is not found in Redis"() {
        given:
            this.redisRepository.findById(this.token) >> Optional.empty()

        when:
            this.service.refreshToken(this.token)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E009
            0 * this.tokenProvider.generateAccessToken(_)
    }

}
