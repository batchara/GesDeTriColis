package com.raoudate.GestionDeTri.Exception;

import lombok.Getter;

public class EntityNotFoundException extends RuntimeException {

    @Getter
    private BusinessErrorCode businessErrorCode;

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityNotFoundException(String message, Throwable cause, BusinessErrorCode businessErrorCode) {
        super(message, cause);
        this.businessErrorCode = businessErrorCode;
    }

    public EntityNotFoundException(String message, BusinessErrorCode businessErrorCode) {
        super(message);
        this.businessErrorCode = businessErrorCode;
    }

  

}
