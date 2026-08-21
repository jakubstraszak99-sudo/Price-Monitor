package com.github.pricemonitor.exception;

import lombok.Getter;

@Getter
public class PmRuntimeException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public PmRuntimeException(final ExceptionCode code) {
        super(String.format("%s: %s", code.name(), code.getMessage()));
        this.exceptionCode = code;
    }

    public PmRuntimeException(final ExceptionCode code, final Throwable cause) {
        super(String.format("%s: %s cause: %s", code.name(), code.getMessage(), cause.getMessage()), cause);
        this.exceptionCode = code;
    }

}
