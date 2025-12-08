package com.raoudate.GestionDeTri.dto.response;

import com.raoudate.GestionDeTri.enums.Permission;
import com.raoudate.GestionDeTri.model.Permissions;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionDTO {
    // Correction : le champ nom doit être de type Permission (l'enum), pas Permissions (l'entité)
    private Permission nom;
    private String description;

    public static PermissionDTO fromEntity(Permissions permissions) {
        if (permissions == null) {
            return null;
        }
        return PermissionDTO.builder()
                .nom(permissions.getNom()) // Correction ici
                .description(permissions.getDescription())
                .build();
    }

    public static Permissions toEntity(PermissionDTO dto) {
        if (dto == null) {
            return null;
        }
        Permissions permissions = new Permissions();
        permissions.setNom(dto.getNom()); // Correction ici
        permissions.setDescription(dto.getDescription());
        return permissions;
    }
}