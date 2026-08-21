package com.github.pricemonitor.request;

public record UserRegisterRequest (
        String username,
        String email,
        String password
) {}
