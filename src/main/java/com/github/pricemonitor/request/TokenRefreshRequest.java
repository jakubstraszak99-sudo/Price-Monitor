package com.github.pricemonitor.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(

        @NotBlank
        String refreshToken
) {}
