package com.raoudate.GestionDeTri.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour représenter une agence proche avec sa distance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgenceProche {
    private Integer agenceId;
    private String code;
    private String nom;
    private String region;
    private String adresse;
    private CoordinatesDTO coordonnees;
    private Double distanceKm;
    private String tempsEstime; // Ex: "15 min", "2h 30min"
}
