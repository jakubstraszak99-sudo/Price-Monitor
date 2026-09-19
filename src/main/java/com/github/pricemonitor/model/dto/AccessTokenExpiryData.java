package com.github.pricemonitor.model.dto;

public record AccessTokenExpiryData(
        String accessToken,
        long accessExpirationSeconds
) {}
