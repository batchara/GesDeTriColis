package com.raoudate.GestionDeTri.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la réponse du scan d'un colis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanColisResponseDTO {
    // OCR
    private String texteExtrait;
    private String adresseDetectee;
    private Boolean ocrSucces;
    
    // Parsing intelligent (nouveauté)
    private DonneesStructureesDTO donneesStructurees;
    
    // Géocodage
    private GeocodingResultDTO geocodage;
    
    // Agences
    private AgenceProche agenceLaPlusProche;
    private List<AgenceProche> autresAgencesProches; // Top 5 des agences les plus proches
    
    // Métadonnées
    private Boolean succes;
    private String message;
    private String messageErreur;
}

