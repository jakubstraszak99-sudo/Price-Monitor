package com.github.pricemonitor.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleInvalidRequest(final Exception exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "INVALID_REQUEST",
                "Invalid request body",
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(PmRuntimeException.class)
    public ResponseEntity<ErrorResponse> handlePmRuntimeException(final PmRuntimeException exception) {
        final ExceptionCode code = exception.getCode();
        final ErrorResponse response = new ErrorResponse(
                code.name(),
                code.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(code.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(final Exception exception) {
        return ResponseEntity.status(500).body(new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occured",
                LocalDateTime.now()
        ));
    }

}
