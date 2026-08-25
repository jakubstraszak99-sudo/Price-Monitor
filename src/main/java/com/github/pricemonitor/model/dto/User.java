package com.github.pricemonitor.model.dto;

import java.util.UUID;

public record User(
        Long id,
        UUID publicId,
        String username,
        String passwordHash,
        String email,
        Boolean verified
) {}
