package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.model.Agences;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenceDTO {

    private Integer id;
    private String nom;
    private String code;
    private String email;
    private String tel;
    private String numTel; // Alias pour tel
    private String quartier;
    private String region;
    private String status; // ACTIVE, INACTIVE
    private AdresseDTO adresse;

    public static AgenceDTO fromEntity(Agences agences) {
        if (agences == null) {
            return null;
        }
        return AgenceDTO.builder()
                .id(agences.getId())
                .nom(agences.getLabel())
                .code(agences.getCode())
                .region(agences.getRegion())
                .email(agences.getEmail())
                .tel(agences.getTel())
                .numTel(agences.getTel())
                .status("ACTIVE") // Par défaut
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
        agences.setTel(dto.getTel() != null ? dto.getTel() : dto.getNumTel());
        agences.setAdresse(AdresseDTO.toEntity(dto.getAdresse()));
        return agences;
    }
}
