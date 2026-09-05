package com.github.pricemonitor.service

import com.github.pricemonitor.properties.AppProperties
import com.github.pricemonitor.service.impl.EmailServiceImpl
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import spock.lang.Specification
import spock.lang.Subject

class EmailServiceSpec extends Specification {

    def mailSender = Mock(JavaMailSender)
    def fromAddress = "noreply@pricemonitor.com"
    def appUrl = "http://appurl"
    def userEmail = "test@example.com"
    def testToken = "test-token-123"
    def appProperties = new AppProperties(new AppProperties.Mail(this.fromAddress), null, this.appUrl)

    @Subject
    def service = new EmailServiceImpl(this.mailSender, this.appProperties)

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
