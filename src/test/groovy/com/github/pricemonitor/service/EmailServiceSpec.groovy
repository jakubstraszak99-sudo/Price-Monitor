package com.github.pricemonitor.service

import com.github.pricemonitor.service.impl.EmailServiceImpl
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject

class EmailServiceSpec extends Specification {

    def mailSender = Mock(JavaMailSender)

    @Subject
    def service = new EmailServiceImpl(this.mailSender)

    def fromAddress = "noreply@pricemonitor.com"
    def appUrl = "http://appurl"
    def userEmail = "test@example.com"
    def testToken = "test-token-123"

    def setup() {
        ReflectionTestUtils.setField(this.service, "from", this.fromAddress)
        ReflectionTestUtils.setField(this.service, "url", this.appUrl)
    }

    def "Should successfully send verification email"() {
        given:
            def mimeMessage = new MimeMessage(null)
            this.mailSender.createMimeMessage() >> mimeMessage

        when:
            this.service.sendVerificationEmail(this.userEmail, this.testToken)

        then:
            1 * this.mailSender.send({ MimeMessage msg ->
                msg.getAllRecipients()[0].toString() == this.userEmail
                msg.getFrom()[0].toString() == this.fromAddress
            })
    }

    def "Should successfully send password reset email"() {
        given:
            def mimeMessage = new MimeMessage(null)
            this.mailSender.createMimeMessage() >> mimeMessage

        when:
            this.service.sendPasswordResetEmail(this.userEmail, this.testToken)

        then:
            1 * this.mailSender.send({ MimeMessage msg ->
                msg.getAllRecipients()[0].toString() == this.userEmail
                msg.getFrom()[0].toString() == this.fromAddress
            })
    }

}
