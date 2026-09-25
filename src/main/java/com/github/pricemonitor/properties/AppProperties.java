package com.github.pricemonitor.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Mail mail,
        Jwt jwt,
        Paths paths,
        Cookie cookie,
        String clientUrl
) {
    public record Mail(String from) {}

    public record Jwt(
            String secret,
            long verificationExpirationMs,
            long accessExpirationMs,
            long refreshExpirationMs
    ) {}

    public record Paths(
            String accessToken,
            String refreshToken,
            String verification,
            String passwordReset,
            String tokenQueryParam
    ) {}

    public record Cookie(
            String accessToken,
            String refreshToken,
            boolean secure
    ) {}

}