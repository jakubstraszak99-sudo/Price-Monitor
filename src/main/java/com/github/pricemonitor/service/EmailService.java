package com.github.pricemonitor.service;

public interface EmailService {

    void sendVerificationEmail(final String to, final String token);

    void sendPasswordResetEmail(final String to, final String token);

}
