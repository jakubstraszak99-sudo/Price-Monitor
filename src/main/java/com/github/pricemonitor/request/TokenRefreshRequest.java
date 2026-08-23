package com.github.pricemonitor.request;

public record TokenRefreshRequest(
        String refreshToken
) {}
