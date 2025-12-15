package com.raoudate.GestionDeTri.exception;


import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

import static com.raoudate.GestionDeTri.exception.BusinessErrorCode.*;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {


        @ExceptionHandler(LockedException.class)

                public  ResponseEntity<ExceptionResponse> handleException(LockedException exp) {
            return ResponseEntity
                    .status(UNAUTHORIZED)
                    .body(
                            ExceptionResponse.builder()
                                    .businessErrorCode(BusinessErrorCode.ACCOUNT_LOCKED.getCode()+"")
                                    .businessErrorDescription(BusinessErrorCode.ACCOUNT_LOCKED.getDescription())
                                    .error(exp.getMessage())
                                    .build()
                    );

        }

    @ExceptionHandler(DisabledException.class)

    public  ResponseEntity<ExceptionResponse> handleException(DisabledException exp) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(BusinessErrorCode.ACCOUNT_DISABLED.getCode()+"")
                                .businessErrorDescription(BusinessErrorCode.ACCOUNT_DISABLED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );

    }

    @ExceptionHandler(BadCredentialsException.class)

    public  ResponseEntity<ExceptionResponse> handleException(BadCredentialsException exp) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(BAD_CREDENTIALS.getCode()+"")
                                .businessErrorDescription(BAD_CREDENTIALS.getDescription())
                                .error(BAD_CREDENTIALS.getDescription())
                                .build()
                );

    }

    @ExceptionHandler(MessagingException.class)

    public  ResponseEntity<ExceptionResponse> handleException(MessagingException exp) {
       
        String userMessage = "Impossible d'envoyer l'email. Veuillez vérifier que votre adresse email est valide et fonctionnelle.";
        
        
        exp.printStackTrace();
        
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .error(userMessage)
                                .build()
                );

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)

    public  ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException exp) {
            Set<String> errors = new HashSet<>();

            exp.getBindingResult().getAllErrors().forEach((error) -> {
                var errorMessage = error.getDefaultMessage();
                errors.add(errorMessage);
            });
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .validationErrors(errors)
                                .build()
                );

    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handleRuntimeException(RuntimeException exp) {
        exp.printStackTrace();
        
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .error(exp.getMessage())
                                .message(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)

    public  ResponseEntity<ExceptionResponse> handleException(Exception exp) {
            // log the exception
            exp.printStackTrace();

        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorDescription("Internal server error, contact the administrator")
                                .error(exp.getMessage())
                                .build()
                );

    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ExceptionResponse> handleException(InvalidTokenException exp) {
        return ResponseEntity
                .status(TOKEN_INVALID.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(TOKEN_INVALID.getCode() + "")
                                .businessErrorDescription(TOKEN_INVALID.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ExceptionResponse> handleException(IllegalStateException exp) {
        return ResponseEntity
                .status(FORBIDDEN)
                .body(
                        ExceptionResponse.builder()
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ExceptionResponse> handleException(TokenExpiredException exp) {
        return ResponseEntity
                .status(TOKEN_EXPIRED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(TOKEN_EXPIRED.getCode() + "")
                                .businessErrorDescription(TOKEN_EXPIRED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponse> handleException(BusinessException exp) {
        return ResponseEntity
                .status(exp.getErrorCode().getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(exp.getErrorCode().getCode() + "")
                                .businessErrorDescription(exp.getErrorCode().getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }
}
