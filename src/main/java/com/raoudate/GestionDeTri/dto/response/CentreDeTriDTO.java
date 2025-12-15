package com.raoudate.GestionDeTri.dto.response;

import com.raoudate.GestionDeTri.model.CentreDeTri;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CentreDeTriDTO {
    private Integer id;
    private String nom;
    private String telephone;
    private String adresseCentre;

    public static CentreDeTriDTO fromEntity(CentreDeTri centre) {
        if (centre == null) {
            return null;
        }

        return CentreDeTriDTO.builder()
                .id(centre.getId())
                .nom(centre.getNom())
                .telephone(centre.getTelephone())
                .adresseCentre(centre.getAdresseCentre())
                .build();
    }

    public static CentreDeTri toEntity(CentreDeTriDTO centreDeTriDTO) {
        if (centreDeTriDTO == null) {
            return null;
        }

        CentreDeTri centre = new CentreDeTri();
        centre.setId(centreDeTriDTO.getId());
        centre.setNom(centreDeTriDTO.getNom());
        centre.setTelephone(centreDeTriDTO.getTelephone());
        centre.setAdresseCentre(centreDeTriDTO.getAdresseCentre());
        return centre;
    }
}