package com.github.pricemonitor.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionCode {

    E001("User not found", NOT_FOUND),
    E002("User is already verified", BAD_REQUEST),
    E003("JWT token date expired", UNAUTHORIZED),
    E004("JWT token is invalid", UNAUTHORIZED),
    E005("Email address already exists", CONFLICT),
    E006("Invalid credentials", UNAUTHORIZED),
    E007("Session expired", UNAUTHORIZED);

    private final String message;
    private final HttpStatus status;

}
