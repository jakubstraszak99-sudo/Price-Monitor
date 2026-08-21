package com.github.pricemonitor.model.dto;

public record UserDto (
        Long id,
        String username,
        String passwordHash,
        String email,
        Boolean verified
) {}
