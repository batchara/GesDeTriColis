package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.Enum.permissionName;
import com.raoudate.GestionDeTri.model.Permissions;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionDTO {

    private permissionName nom;

    private String description;

    public static PermissionDTO fromEntity(Permissions permissions) {
        if (permissions == null) {
            return null;
        }
        return PermissionDTO.builder()
                .nom(permissions.getNom())
                .description(permissions.getDescription())
                .build();
    }

    public static Permissions toEntity(PermissionDTO dto) {
        if (dto == null) {
            return null;
        }
        Permissions permissions = new Permissions();
        permissions.setNom(dto.getNom());
        permissions.setDescription(dto.getDescription());
        return permissions;
    }
}