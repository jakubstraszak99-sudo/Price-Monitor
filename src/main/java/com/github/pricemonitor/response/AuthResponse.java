package com.github.pricemonitor.response;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
