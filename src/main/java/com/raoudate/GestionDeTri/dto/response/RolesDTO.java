package com.raoudate.GestionDeTri.dto.response;

import com.raoudate.GestionDeTri.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RolesDTO {
    private Integer id;
    private String name;

    // Conversion de l'entité vers le DTO
    public static RolesDTO fromEntity(Role role) {
        if (role == null) {
            return null;
        }

        return RolesDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }

    // Conversion de DTO vers l'entité
    public static Role toEntity(RolesDTO rolesDTO) {
        if (rolesDTO == null) {
            return null;
        }

        Role role = new Role();
        role.setId(rolesDTO.getId());
        role.setName(rolesDTO.getName());
        return role;
    }
}