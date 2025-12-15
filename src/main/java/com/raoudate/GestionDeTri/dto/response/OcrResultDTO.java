package com.raoudate.GestionDeTri.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le résultat de l'extraction OCR simple
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OcrResultDTO {
    
    private boolean succes;

    private String texte;
    
    private String message;
}
