package com.raoudate.GestionDeTri.exception;

import lombok.Getter;

import java.util.List;

public class InvalidEnityException extends RuntimeException     {
    @Getter
    private BusinessErrorCode businessErrorCode;

    @Getter
    private List<String> errors;


    public InvalidEnityException(String message) {
        super(message);
    }

    public InvalidEnityException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidEnityException(String message, Throwable cause, BusinessErrorCode businessErrorCode) {
        super(message, cause);
        this.businessErrorCode = businessErrorCode;
    }

    public InvalidEnityException(String message, BusinessErrorCode businessErrorCode, List<String> errors) {
        super(message);
        this.businessErrorCode = businessErrorCode;
        this.errors = errors;
    }
}
