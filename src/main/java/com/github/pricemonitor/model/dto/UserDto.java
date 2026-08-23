package com.github.pricemonitor.model.dto;

import java.util.UUID;

public record UserDto (
        Long id,
        UUID publicId,
        String username,
        String passwordHash,
        String email,
        Boolean verified
) {}
