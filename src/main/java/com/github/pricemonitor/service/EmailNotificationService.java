package com.github.pricemonitor.service;

public interface EmailNotificationService {

    void sendVerificationEmail(final String to, final String token);

    void sendPasswordResetEmail(final String to, final String token);

    void sendAlertNotificationEmail(final String to, final String url);

}
