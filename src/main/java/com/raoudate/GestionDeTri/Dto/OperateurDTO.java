package com.raoudate.GestionDeTri.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperateurDTO {

    private Integer id;

    public static OperateurDTO fromEntity(com.raoudate.GestionDeTri.model.Operateur operateur) {
        if (operateur == null) {
            return null;
        }
        return OperateurDTO.builder()
                .id(operateur.getId())
                .build();
    }

    public static com.raoudate.GestionDeTri.model.Operateur toEntity(OperateurDTO dto) {
        if (dto == null) {
            return null;
        }
        com.raoudate.GestionDeTri.model.Operateur operateur = new com.raoudate.GestionDeTri.model.Operateur();
        operateur.setId(dto.getId());
        return operateur;
    }
}