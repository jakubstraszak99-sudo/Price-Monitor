package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.properties.AppProperties;
import com.github.pricemonitor.repository.UserRepository;
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

    private static final String VERIFICATION_SUBJECT = "Price Monitor - Weryfikacja konta";
    private static final String PASSWORD_RESET_SUBJECT = "Price Monitor - Prośba o zmianę hasła";
    private static final String ALERT_SUBJECT = "Price Monitor - Alert cenowy!";

    private static final String VERIFICATION_TEMPLATE = """
            <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2 style="color: #4f46e5;">Witaj w serwisie Price Monitor!</h2>
                    <p>Wciśnij przycisk poniżej, aby zweryfikować swoje konto:</p>
                    <p style="margin: 20px 0;">
                        <a href="%s" style="display: inline-block; padding: 10px 20px; color: #ffffff; background-color: #007bff; text-decoration: none; border-radius: 5px; font-weight: bold;">
                            Zweryfikuj Konto
                        </a>
                    </p>
                    <p style="font-size: 12px; color: #777;">
                        Jeśli nie rejestrowałeś konta, zignoruj tę wiadomość.
                    </p>
                </body>
            </html>
            """;

    private static final String PASSWORD_RESET_TEMPLATE = """
            <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <p>Otrzymaliśmy prośbę o zresetowanie hasła do Twojego konta w serwisie Price Monitor.</p>
                    <p>Kliknij przycisk poniżej, aby ustawić nowe hasło:</p>
                    <p style="margin: 20px 0;">
                        <a href="%s" style="display: inline-block; padding: 10px 20px; color: #ffffff; background-color: #dc3545; text-decoration: none; border-radius: 5px; font-weight: bold;">
                            Zresetuj Hasło
                        </a>
                    </p>
                    <p style="font-size: 12px; color: #777;">
                        Jeśli nie wysyłałeś prośby o zresetowanie hasła, możesz spokojnie zignorować tę wiadomość. Twoje obecne hasło pozostanie bez zmian.
                    </p>
                </body>
            </html>
            """;

    private static final String ALERT_TEMPLATE = """
            <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2 style="color: #4f46e5;">ALERT CENOWY!</h2>
                    <p>Cena przedmiotu, który monitorujesz, spadła poniżej ustalonej przez Ciebie kwoty!</p>
                    <p style="margin: 20px 0;">
                        <a href="%s" style="display: inline-block; padding: 10px 20px; color: #ffffff; background-color: #4f46e5; text-decoration: none; border-radius: 5px; font-weight: bold;">
                            Sprawdź swój przedmiot
                        </a>
                    </p>
                </body>
            </html>
            """;

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;
    private final UserRepository userRepository;

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
        // Recheck the preference in case it changed while the event was queued.
        if (!this.userRepository.existsByEmailAndEmailAlertsEnabledTrue(to)) {
            return;
        }

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
