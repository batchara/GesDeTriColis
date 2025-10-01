package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.model.Agences;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgenceDTO {

    private String nom;
    private String prenom;
    private String email;
    private String numTel;
    private AdresseDTO adresse;

    public static AgenceDTO fromEntity(Agences agences) {
        if (agences == null) {
            return null;
        }
        return AgenceDTO.builder()
                .nom(agences.getNom())
                .prenom(agences.getPrenom())
                .email(agences.getEmail())
                .numTel(agences.getNumTel())
                .adresse(AdresseDTO.fromEntity(agences.getAdresse()))
                .build();
    }

    public static Agences toEntity(AgenceDTO dto) {
        if (dto == null) {
            return null;
        }
        Agences agences = new Agences();
        agences.setNom(dto.getNom());
        agences.setPrenom(dto.getPrenom());
        agences.setEmail(dto.getEmail());
        agences.setNumTel(dto.getNumTel());
        agences.setAdresse(AdresseDTO.toEntity(dto.getAdresse()));
        return agences;
    }
}
