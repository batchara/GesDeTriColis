package com.raoudate.GestionDeTri.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class AdminDTO {
    private Integer id;

    public static AdminDTO fromEntity(com.raoudate.GestionDeTri.model.Admin admin) {
        if (admin == null) {
            return null;
        }
        return AdminDTO.builder()
                .id(admin.getId())
                .build();
    }

    public static com.raoudate.GestionDeTri.model.Admin toEntity(AdminDTO dto) {
        if (dto == null) {
            return null;
        }
        com.raoudate.GestionDeTri.model.Admin admin = new com.raoudate.GestionDeTri.model.Admin();
        admin.setId(dto.getId());
        return admin;
    }


}
