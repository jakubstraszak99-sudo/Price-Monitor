package com.github.pricemonitor.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {

    E001("User not found", HttpStatus.NOT_FOUND),
    E002("User is already verified", HttpStatus.BAD_REQUEST),
    E003("JWT token date expired", HttpStatus.UNAUTHORIZED),
    E004("JWT token is invalid", HttpStatus.UNAUTHORIZED),
    E005("Email address already exists", HttpStatus.CONFLICT),
    E006("Invalid credentials", HttpStatus.UNAUTHORIZED),
    E007("Session expired", HttpStatus.UNAUTHORIZED),
    E008("Product or price could not be identified with the provided URL", HttpStatus.UNPROCESSABLE_CONTENT),
    E009("No URL support", HttpStatus.UNPROCESSABLE_CONTENT),
    E010("A network problem occurred while connecting", HttpStatus.BAD_GATEWAY);

    private final String message;
    private final HttpStatus status;

}
