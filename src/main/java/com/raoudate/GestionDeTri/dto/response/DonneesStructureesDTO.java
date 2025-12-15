package com.raoudate.GestionDeTri.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonneesStructureesDTO {
    
    private String nom;
    
    private String prenom;
    
    private String code;
    
    private String telephone;
    
    private String email;
  
    private String adresse;
    
    private String ville;
    
    private String quartier;
   
    private String region;
    
    private String pays;
    
    private String nomExpediteur;
    
    private String telephoneExpediteur;
    
    private String adresseExpediteur;
    
    private String codeSuivi;
 
    private Double poids;
    
    private String notes;
    
    private Double confidence;
}
