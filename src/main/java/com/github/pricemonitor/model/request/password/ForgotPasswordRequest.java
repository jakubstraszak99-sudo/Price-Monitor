package com.github.pricemonitor.model.request.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(

        @NotBlank
        @Email
        String email
) {}
