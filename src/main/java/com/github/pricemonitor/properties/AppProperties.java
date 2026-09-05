package com.github.pricemonitor.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Mail mail,
        Jwt jwt,
        String clientUrl
) {
    public record Mail(String from) {}

    public record Jwt(
            String secret,
            long verificationExpirationMs,
            long accessExpirationMs,
            long refreshExpirationMs
    ) {}
}