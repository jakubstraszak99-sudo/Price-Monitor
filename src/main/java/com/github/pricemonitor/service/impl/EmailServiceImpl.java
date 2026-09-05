package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.properties.AppProperties;
import com.github.pricemonitor.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import static com.github.pricemonitor.utils.AuthenticationUtil.PASSWORD_RESET_PATH;
import static com.github.pricemonitor.utils.AuthenticationUtil.VERIFICATION_TOKEN_PATH;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String VERIFICATION_SUBJECT = "Price Monitor - Account Verification";
    private static final String PASSWORD_RESET_SUBJECT = "Price Monitor - Password Reset Request";

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

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Override
    public void sendVerificationEmail(final String to, final String token) {
        final String htmlContent = VERIFICATION_TEMPLATE.formatted(this.buildLink(VERIFICATION_TOKEN_PATH, token));
        this.sendEmail(to, VERIFICATION_SUBJECT, htmlContent);
        log.info("Verification email sent to: {}", to);
    }


    @Override
    public void sendPasswordResetEmail(final String to, final String token) {
        final String htmlContent = PASSWORD_RESET_TEMPLATE.formatted(this.buildLink(PASSWORD_RESET_PATH, token));
        this.sendEmail(to, PASSWORD_RESET_SUBJECT, htmlContent);
        log.info("Password reset email sent to: {}", to);
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

    private String buildLink(final String apiPath, final String token) {
        return this.appProperties.clientUrl() + apiPath + token;
    }

}
