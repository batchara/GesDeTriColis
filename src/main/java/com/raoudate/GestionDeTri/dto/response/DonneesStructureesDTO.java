package com.raoudate.GestionDeTri.dto.response;

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
     * Nom du destinataire
     */
    private String nom;
    
    /**
     * Prénom du destinataire (si disponible)
     */
    private String prenom;
    
    /**
     * Code de l'agence (si applicable)
     */
    private String code;
    
    /**
     * Numéro de téléphone du destinataire
     */
    private String telephone;
    
    /**
     * Email du destinataire (si présent)
     */
    private String email;
    
    /**
     * Adresse complète du destinataire formatée
     */
    private String adresse;
    
    /**
     * Ville du destinataire
     */
    private String ville;
    
    /**
     * Quartier du destinataire
     */
    private String quartier;
    
    /**
     * Région du destinataire
     */
    private String region;
    
    /**
     * Pays (normalement "Togo")
     */
    private String pays;
    
    
    
    /**
     * Nom de l'expéditeur (entreprise ou personne)
     */
    private String nomExpediteur;
    
    /**
     * Téléphone de l'expéditeur
     */
    private String telephoneExpediteur;
    
    /**
     * Adresse de l'expéditeur
     */
    private String adresseExpediteur;
    
    
    
    /**
     * Code de suivi du bordereau (ex: COL-TG-2025-001234)
     */
    private String codeSuivi;
    
    /**
     * Poids du colis en kilogrammes (ex: 2.5)
     */
    private Double poids;
    
    /**
     * Informations additionnelles
     */
    private String notes;
    
    /**
     * Niveau de confiance du parsing (0.0 à 1.0)
     */
    private Double confidence;
}
