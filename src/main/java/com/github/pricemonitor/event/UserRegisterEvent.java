package com.github.pricemonitor.event;

public record UserRegisterEvent(
        String email,
        String verificationToken
) {}
