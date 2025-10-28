package com.raoudate.GestionDeTri.Exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    
    private final BusinessErrorCode errorCode;
    
    public BusinessException(BusinessErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
    
    public BusinessException(BusinessErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(BusinessErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
