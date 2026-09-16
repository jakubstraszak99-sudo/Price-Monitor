package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.properties.AppProperties;
import com.github.pricemonitor.service.EmailNotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private static final String VERIFICATION_SUBJECT = "Price Monitor - Account Verification";
    private static final String PASSWORD_RESET_SUBJECT = "Price Monitor - Password Reset Request";
    private static final String ALERT_SUBJECT = "Price Monitor - Price Alert";

    private static final String VERIFICATION_TEMPLATE = """
            <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2>Welcome to Price Monitor!</h2>
                    <p>Please verify your account by clicking the button below:</p>
                    <p style="margin: 20px 0;">
                        <a href="%s" style="display: inline-block; padding: 10px 20px; color: #ffffff; background-color: #007bff; text-decoration: none; border-radius: 5px; font-weight: bold;">
                            Verify Account
                        </a>
                    </p>
                    <p style="font-size: 12px; color: #777;">
                        If you did not register for this account, please ignore this email.
                    </p>
                </body>
            </html>
            """;

    private static final String PASSWORD_RESET_TEMPLATE = """
            <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2>Password Reset</h2>
                    <p>We received a request to reset your password for your Price Monitor account.</p>
                    <p>Click the button below to set a new password:</p>
                    <p style="margin: 20px 0;">
                        <a href="%s" style="display: inline-block; padding: 10px 20px; color: #ffffff; background-color: #dc3545; text-decoration: none; border-radius: 5px; font-weight: bold;">
                            Reset Password
                        </a>
                    </p>
                    <p style="font-size: 12px; color: #777;">
                        If you did not request a password reset, you can safely ignore this email. Your password will remain unchanged.
                    </p>
                </body>
            </html>
            """;

    private static final String ALERT_TEMPLATE = """
            <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2>Price Alert!</h2>
                    <p>Price of the product you're tracking has dropped below the price you set!</p>
                    <p style="margin: 20px 0;">
                        <a href="%s" style="display: inline-block; padding: 10px 20px; color: #ffffff; background-color: #4f46e5; text-decoration: none; border-radius: 5px; font-weight: bold;">
                            Check your product
                        </a>
                    </p>
                </body>
            </html>
            """;

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Override
    public void sendVerificationEmail(final String to, final String token) {
        final String link = this.buildLink(this.appProperties.paths().verification(), token);
        final String htmlContent = VERIFICATION_TEMPLATE.formatted(link);
        this.sendEmail(to, VERIFICATION_SUBJECT, htmlContent);
        log.debug("Verification email sent to: {}", to);
    }

    @Override
    public void sendPasswordResetEmail(final String to, final String token) {
        final String link = this.buildLink(this.appProperties.paths().passwordReset(), token);
        final String htmlContent = PASSWORD_RESET_TEMPLATE.formatted(link);
        this.sendEmail(to, PASSWORD_RESET_SUBJECT, htmlContent);
        log.debug("Password reset email sent to: {}", to);
    }

    @Override
    public void sendAlertNotificationEmail(final String to, final String url) {
        final String htmlContent = ALERT_TEMPLATE.formatted(url);
        this.sendEmail(to, ALERT_SUBJECT, htmlContent);
        log.debug("Alert notification email sent to: {}", to);
    }

    private void sendEmail(final String to, final String subject, final String htmlContent) {
        try {
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(this.appProperties.mail().from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            this.mailSender.send(mimeMessage);
        } catch (final MessagingException e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    private String buildLink(final String path, final String token) {
        return UriComponentsBuilder
                .fromUriString(this.appProperties.clientUrl())
                .path(path)
                .queryParam(this.appProperties.paths().tokenQueryParam(), token)
                .build()
                .toUriString();
    }

}
