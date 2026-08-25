package com.github.pricemonitor.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

    E001("User not found", HttpStatus.NOT_FOUND),
    E002("User is already verified", HttpStatus.BAD_REQUEST),
    E003("Invalid current password", HttpStatus.BAD_REQUEST),
    E004("New password is the same as the old one", HttpStatus.BAD_REQUEST),
    E005("JWT token date expired", HttpStatus.UNAUTHORIZED),
    E006("JWT token is invalid", HttpStatus.UNAUTHORIZED),
    E007("Email address already exists", HttpStatus.CONFLICT),
    E008("Invalid credentials", HttpStatus.UNAUTHORIZED),
    E009("Session expired", HttpStatus.UNAUTHORIZED),
    E010("Product or price could not be identified with the provided URL", HttpStatus.UNPROCESSABLE_CONTENT),
    E011("No URL support", HttpStatus.UNPROCESSABLE_CONTENT),
    E012("A network problem occurred while connecting", HttpStatus.BAD_GATEWAY),
    E013("Failed to receive data", HttpStatus.GATEWAY_TIMEOUT);

    private final String message;
    private final HttpStatus status;

}
