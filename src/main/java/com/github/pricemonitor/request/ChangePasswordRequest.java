package com.github.pricemonitor.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank
        @Size(min = 8)
        String password
) {}
