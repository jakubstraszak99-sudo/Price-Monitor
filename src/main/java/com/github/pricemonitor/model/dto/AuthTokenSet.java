package com.github.pricemonitor.model.dto;

public record AuthTokenSet(
        String accessToken,
        String refreshToken,
        long accessExpirationSeconds,
        long refreshExpirationSeconds
) {}