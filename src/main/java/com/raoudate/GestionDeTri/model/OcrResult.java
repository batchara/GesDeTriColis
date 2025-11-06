package com.raoudate.GestionDeTri.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Résultat de la reconnaissance OCR
 * Contient le texte extrait et les informations d'adresse identifiées
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {
    /**
     * Texte brut extrait de l'image
     */
    private String rawText;
    
    /**
     * Code de suivi du colis
     */
    private String codeSuivi;
    
    /**
     * Nom de l'expéditeur
     */
    private String nomExpediteur;
    
    /**
     * Nom complet du destinataire extrait
     */
    private String nomDestinataire;
    
    /**
     * Poids du colis
     */
    private String poids;
    
    /**
     * Adresse complète extraite
     */
    private String adresse;
    
    /**
     * Ville extraite
     */
    private String ville;
    
    /**
     * Code postal extrait
     */
    private String codePostal;
    
    /**
     * Pays extrait (par défaut TOGO)
     */
    private String pays;
    
    /**
     * Numéro de téléphone extrait
     */
    private String telephone;
    
    /**
     * Niveau de confiance de l'OCR (0-100)
     */
    private Integer confidence;
    
    /**
     * Indique si l'extraction a réussi
     */
    private boolean success;
    
    /**
     * Message d'erreur en cas d'échec
     */
    private String errorMessage;
    
    /**
     * Temps de traitement en millisecondes
     */
    private Long processingTime;
}
