package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.model.Utilisateur;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class UtilisateurDTO {
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private Integer numTel;
    private List<String> roles;

    // Conversion de l'entité vers le DTO
    public static UtilisateurDTO fromEntity(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        return UtilisateurDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .numTel(utilisateur.getNumTel())
                .roles(utilisateur.getRoles().stream()
                        .map(role -> role.getNom()) // Extraire juste les noms des rôles
                        .collect(Collectors.toList()))
                .build();
    }

    // Conversion de DTO vers l'entité
    public static Utilisateur toEntity(UtilisateurDTO utilisateurDTO) {
        if (utilisateurDTO == null) {
            return null;
        }

        Utilisateur utilisateur = new Utilisateur() {
            // On définit une classe anonyme si Utilisateur est abstract
        };
        utilisateur.setId(utilisateurDTO.getId());
        utilisateur.setNom(utilisateurDTO.getNom());
        utilisateur.setPrenom(utilisateurDTO.getPrenom());
        utilisateur.setEmail(utilisateurDTO.getEmail());
        utilisateur.setNumTel(utilisateurDTO.getNumTel());
        // Note : Les rôles doivent être récupérés via la base de données.
        return utilisateur;
    }
}