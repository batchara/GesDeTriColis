package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.model.Agences;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgenceDTO {

    private String nom;
    private String code;
    private String email;
    private String tel;
    private String quartier;
    private String region;
    private AdresseDTO adresse;

    public static AgenceDTO fromEntity(Agences agences) {
        if (agences == null) {
            return null;
        }
        return AgenceDTO.builder()
                .nom(agences.getLabel())
                .code(agences.getCode())
                .region(agences.getRegion())
                .email(agences.getEmail())
                .tel(agences.getTel())
                .adresse(AdresseDTO.fromEntity(agences.getAdresse()))
                .build();
    }

    public static Agences toEntity(AgenceDTO dto) {
        if (dto == null) {
            return null;
        }
        Agences agences = new Agences();
        agences.setLabel(dto.getNom());
        agences.setCode(dto.getCode());
        agences.setRegion(dto.getRegion());
        agences.setEmail(dto.getEmail());
        agences.setTel(dto.getTel());
        agences.setAdresse(AdresseDTO.toEntity(dto.getAdresse()));
        return agences;
    }
}
