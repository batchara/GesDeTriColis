package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.model.BoitePostale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoitePostaleDTO {

    private Integer id;
    private String libelle;
    private Integer capacite;
    
    // Informations de l'agence
    private Integer agenceId;
    private String agenceNom;
    private String agenceCode;
    
    // Informations d'audit
    private Instant dateCreation;
    private Instant dateModification;
    private String createdBy;
    private String lastModifiedBy;

    public static BoitePostaleDTO fromEntity(BoitePostale boitePostale) {
        if (boitePostale == null) {
            return null;
        }
        return BoitePostaleDTO.builder()
                .id(boitePostale.getId())
                .libelle(boitePostale.getLibelle())
                .capacite(boitePostale.getCapacite())
                .agenceId(boitePostale.getAgence() != null ? boitePostale.getAgence().getId() : null)
                .agenceNom(boitePostale.getAgence() != null ? boitePostale.getAgence().getLabel() : null)
                .agenceCode(boitePostale.getAgence() != null ? boitePostale.getAgence().getCode() : null)
                .dateCreation(boitePostale.getDateCreation())
                .dateModification(boitePostale.getDateModification())
                .createdBy(boitePostale.getCreatedBy())
                .lastModifiedBy(boitePostale.getLastModifiedBy())
                .build();
    }

    public static BoitePostale toEntity(BoitePostaleDTO dto) {
        if (dto == null) {
            return null;
        }
        BoitePostale boitePostale = new BoitePostale();
        boitePostale.setId(dto.getId());
        boitePostale.setLibelle(dto.getLibelle());
        boitePostale.setCapacite(dto.getCapacite());
        // L'agence sera définie dans le service
        return boitePostale;
    }
}
