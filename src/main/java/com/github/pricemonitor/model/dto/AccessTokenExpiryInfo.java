package com.github.pricemonitor.model.dto;

public record AccessTokenExpiryInfo(
        String accessToken,
        long accessExpirationSeconds
) {}
