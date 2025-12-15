package com.raoudate.GestionDeTri.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
    private String tempsEstime; 
}
