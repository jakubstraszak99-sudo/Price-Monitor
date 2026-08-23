package com.github.pricemonitor.request;

public record UserLoginRequest(
        String login,
        String password
) {}