package com.github.pricemonitor.model.request.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(

        @NotBlank
        String oldPassword,

        @NotBlank
        @Size(min = 8)
        String newPassword

) {}