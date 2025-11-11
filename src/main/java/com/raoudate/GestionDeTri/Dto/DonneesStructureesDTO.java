package com.raoudate.GestionDeTri.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les données structurées extraites du texte OCR via LLM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonneesStructureesDTO {
    
    /**
     * Nom du destinataire ou de l'agence
     */
    private String nom;
    
    /**
     * Code de l'agence (si applicable)
     */
    private String code;
    
    /**
     * Numéro de téléphone
     */
    private String telephone;
    
    /**
     * Email (si présent)
     */
    private String email;
    
    /**
     * Adresse complète formatée
     */
    private String adresse;
    
    /**
     * Ville
     */
    private String ville;
    
    /**
     * Quartier
     */
    private String quartier;
    
    /**
     * Région
     */
    private String region;
    
    /**
     * Pays (normalement "Togo")
     */
    private String pays;
    
    /**
     * Informations additionnelles
     */
    private String notes;
    
    /**
     * Niveau de confiance du parsing (0.0 à 1.0)
     */
    private Double confidence;
}
