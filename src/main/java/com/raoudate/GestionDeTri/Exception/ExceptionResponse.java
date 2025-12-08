package com.raoudate.GestionDeTri.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)

public class ExceptionResponse {

    private String businessErrorCode;
    private String businessErrorDescription;
    private String error;
    private String message; // Alias pour error, pour compatibilité frontend
    private Set<String> validationErrors;
    private Map<String, String> errors;

}
