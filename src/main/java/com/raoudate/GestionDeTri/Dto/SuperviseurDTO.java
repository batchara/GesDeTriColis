package com.raoudate.GestionDeTri.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuperviseurDTO {

    private Integer id;

    public static SuperviseurDTO fromEntity(com.raoudate.GestionDeTri.model.Superviseur superviseur) {
        if (superviseur == null) {
            return null;
        }
        return SuperviseurDTO.builder()
                .id(superviseur.getId())
                .build();
    }

    public static com.raoudate.GestionDeTri.model.Superviseur toEntity(SuperviseurDTO dto) {
        if (dto == null) {
            return null;
        }
        com.raoudate.GestionDeTri.model.Superviseur superviseur = new com.raoudate.GestionDeTri.model.Superviseur();
        superviseur.setId(dto.getId());
        return superviseur;
    }
}
