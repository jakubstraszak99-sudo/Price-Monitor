package com.github.pricemonitor.model.request.user;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(

        @NotBlank
        String login,

        @NotBlank
        String password

) {}