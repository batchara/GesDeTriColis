package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.model.Agences;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    private String region;
    private String adresseComplete;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status; // ACTIVE, INACTIVE
    private String codeBureau;

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
                .adresseComplete(agences.getAdresseComplete())
                .latitude(agences.getLatitude())
                .longitude(agences.getLongitude())
                .status(agences.getStatus() != null ? agences.getStatus() : "ACTIVE")
                .codeBureau(agences.getCodeBureau())
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
        agences.setAdresseComplete(dto.getAdresseComplete());
        agences.setLatitude(dto.getLatitude());
        agences.setLongitude(dto.getLongitude());
        agences.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        agences.setCodeBureau(dto.getCodeBureau());
        return agences;
    }
}
