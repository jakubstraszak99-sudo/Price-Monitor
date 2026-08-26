package com.github.pricemonitor.service

import com.github.pricemonitor.exception.ExceptionCode
import com.github.pricemonitor.exception.PmRuntimeException
import com.github.pricemonitor.kafka.KafkaEventPublisher
import com.github.pricemonitor.model.entity.UserEntity
import com.github.pricemonitor.repository.UserRepository
import com.github.pricemonitor.security.TokenProvider
import com.github.pricemonitor.service.impl.UserServiceImpl
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification
import spock.lang.Subject

class UserServiceSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def tokenProvider = Mock(TokenProvider)
    def eventPublisher = Mock(KafkaEventPublisher)
    def passwordEncoder = Mock(PasswordEncoder)

    @Subject
    def service = new UserServiceImpl(this.userRepository, this.tokenProvider, this.eventPublisher, this.passwordEncoder)

    def userId = UUID.randomUUID()
    def userEmail = "test@example.com"
    def oldHash = "old_encoded_password"

    def "Should return user entity when valid publicId is provided"() {
        given:
            def user = new UserEntity(publicId: this.userId, email: this.userEmail)
            this.userRepository.findByPublicId(this.userId) >> Optional.of(user)

        when:
            def result = this.service.getUser(this.userId)

        then:
            result == user
    }

    def "Should throw exception when user is not found by publicId"() {
        given:
            this.userRepository.findByPublicId(this.userId) >> Optional.empty()

        when: "getting user"
            this.service.getUser(this.userId)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E001
    }

    def "Should update password properly"() {
        given:
            def user = new UserEntity(publicId: this.userId, passwordHash: this.oldHash)
            def oldPassword = "oldSecretPassword"
            def newPassword = "newSecretPassword"
            def newHash = "new_encoded_password"

            this.userRepository.findByPublicId(this.userId) >> Optional.of(user)
            this.passwordEncoder.matches(oldPassword, this.oldHash) >> true
            this.passwordEncoder.matches(newPassword, this.oldHash) >> false
            this.passwordEncoder.encode(newPassword) >> newHash

        when:
            this.service.updatePassword(this.userId, oldPassword, newPassword)

        then:
            user.getPasswordHash() == newHash
            1 * this.userRepository.save(user)
    }

    def "Should throw exception if old password does not match"() {
        given:
            def user = new UserEntity(publicId: this.userId, passwordHash: this.oldHash)
            this.userRepository.findByPublicId(this.userId) >> Optional.of(user)

            def oldPassword = "wrongPassword"
            this.passwordEncoder.matches(oldPassword, this.oldHash) >> false

        when:
            this.service.updatePassword(this.userId, oldPassword, "newPassword")

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E003
            0 * this.userRepository.save(_)
    }

    def "Should throw exception if new password is the same as the old one"() {
        given:
            def user = new UserEntity(publicId: this.userId, passwordHash: this.oldHash)
            this.userRepository.findByPublicId(this.userId) >> Optional.of(user)

            def password = "samePassword"
            this.passwordEncoder.matches(password, this.oldHash) >> true

        when:
            this.service.updatePassword(this.userId, password, password)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E004
            0 * this.userRepository.save(_)
    }

    def "Should generate token and publish event"() {
        given:
            def user = new UserEntity(publicId: this.userId, email: this.userEmail)
            this.userRepository.findByEmail(this.userEmail) >> Optional.of(user)

            def resetToken = "reset-token-123"
            this.tokenProvider.generateVerificationToken(this.userId) >> resetToken

        when:
            this.service.forgotPassword(this.userEmail)

        then:
            1 * this.eventPublisher.publish(_, this.userEmail, { event ->
                event.email() == this.userEmail && event.token() == resetToken
            })
    }

    def "Should reset password using valid token"() {
        given:
            def resetToken = "valid-reset-token"
            def newPassword = "brandNewPassword"
            def newHash = "brand_new_hash"

            this.tokenProvider.extractUserPublicId(resetToken) >> this.userId

            def user = new UserEntity(publicId:  this.userId, passwordHash:  this.oldHash)
            this.userRepository.findByPublicId(this.userId) >> Optional.of(user)

            this.passwordEncoder.encode(newPassword) >> newHash

        when:
            this.service.resetPassword(resetToken, newPassword)

        then:
            user.getPasswordHash() == newHash
            1 * this.userRepository.save(user)
    }

}
