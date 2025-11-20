package com.raoudate.GestionDeTri.Dto;

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
    
    /**
     * Succès de l'extraction
     */
    private boolean succes;
    
    /**
     * Texte extrait de l'image
     */
    private String texte;
    
    /**
     * Message informatif
     */
    private String message;
}
