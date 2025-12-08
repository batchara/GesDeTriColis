package com.raoudate.GestionDeTri.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le résultat du géocodage d'une adresse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeocodingResultDTO {
    private String adresseOriginale;
    private String adresseFormattee;
    private CoordinatesDTO coordonnees;
    private Boolean succes;
    private String messageErreur;
}
