package com.github.pricemonitor.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionCode {

    E001("User not found"),
    E002("User is already verified"),
    E003("JWT token date expired"),
    E004("JWT token is invalid"),
    E005("Email address already exists");

    private final String message;

}
