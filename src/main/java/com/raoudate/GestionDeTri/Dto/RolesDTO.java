package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.model.Roles;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RolesDTO {
    private Integer id;
    private String nom;

    // Conversion de l'entité vers le DTO
    public static RolesDTO fromEntity(Roles roles) {
        if (roles == null) {
            return null;
        }

        return RolesDTO.builder()
                .id(roles.getId())
                .nom(roles.getNom())
                .build();
    }

    // Conversion de DTO vers l'entité
    public static Roles toEntity(RolesDTO rolesDTO) {
        if (rolesDTO == null) {
            return null;
        }

        Roles roles = new Roles();
        roles.setId(rolesDTO.getId());
        roles.setNom(rolesDTO.getNom());
        return roles;
    }
}