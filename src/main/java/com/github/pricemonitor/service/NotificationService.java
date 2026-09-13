package com.github.pricemonitor.service;

public interface NotificationService {

    void sendVerificationEmail(final String to, final String token);

    void sendPasswordResetEmail(final String to, final String token);

}
