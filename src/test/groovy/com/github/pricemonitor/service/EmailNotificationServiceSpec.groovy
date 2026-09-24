package com.github.pricemonitor.service

import com.github.pricemonitor.properties.AppProperties
import com.github.pricemonitor.repository.UserRepository
import com.github.pricemonitor.service.impl.EmailNotificationServiceImpl
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import spock.lang.Specification
import spock.lang.Subject

class EmailNotificationServiceSpec extends Specification {

    def mailSender = Mock(JavaMailSender)
    def userRepository = Mock(UserRepository)
    def fromAddress = "noreply@pricemonitor.com"
    def appUrl = "http://appurl"
    def userEmail = "test@example.com"
    def testToken = "test-token-123"
    def appProperties = new AppProperties(
            new AppProperties.Mail(this.fromAddress),
            null,
            new AppProperties.Paths(
                    null,
                    null,
                    "/verify",
                    "/reset-password",
                    "token"
            ),
            null,
            this.appUrl
    )

    @Subject
    def service = new EmailNotificationServiceImpl(this.mailSender, this.appProperties, this.userRepository)

    def setup() {
        this.mailSender.createMimeMessage() >> new MimeMessage(null)
    }

    def "Should successfully send verification email"() {
        when:
            this.service.sendVerificationEmail(this.userEmail, this.testToken)

        then:
            0 * this.userRepository._
            1 * this.mailSender.send({ MimeMessage msg ->
                msg.getAllRecipients()[0].toString() == this.userEmail
                msg.getFrom()[0].toString() == this.fromAddress
            })
    }

    def "Should successfully send password reset email"() {
        when:
            this.service.sendPasswordResetEmail(this.userEmail, this.testToken)

        then:
            0 * this.userRepository._
            1 * this.mailSender.send({ MimeMessage msg ->
                msg.getAllRecipients()[0].toString() == this.userEmail
                msg.getFrom()[0].toString() == this.fromAddress
            })
    }

    def "Should successfully send alert notification email"() {
        given:
            this.userRepository.existsByEmailAndEmailAlertsEnabledTrue(this.userEmail) >> true

        when:
            this.service.sendAlertNotificationEmail(this.userEmail, "https://testurl")

        then:
            1 * this.mailSender.send({ MimeMessage msg ->
                msg.getAllRecipients()[0].toString() == this.userEmail
                msg.getFrom()[0].toString() == this.fromAddress
            })
    }

    def "Should skip queued alert emails if the user opted out or no longer exists"() {
        given:
            this.userRepository.existsByEmailAndEmailAlertsEnabledTrue(this.userEmail) >> false

        when:
            this.service.sendAlertNotificationEmail(this.userEmail, "https://testurl")

        then:
            0 * this.mailSender.createMimeMessage()
            0 * this.mailSender.send(_)
    }

}
