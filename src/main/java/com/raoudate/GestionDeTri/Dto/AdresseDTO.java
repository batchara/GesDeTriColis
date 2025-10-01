package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.Enum.TypeAdresse;
import com.raoudate.GestionDeTri.model.Adresse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdresseDTO {

    private String adresseComplete;
    private String quartier;
    private String ville;
    private String codePostale;
    private TypeAdresse typeAdresse;
    private Double latitude;
    private Double longitude;

    public static AdresseDTO fromEntity(Adresse adresse) {
        if (adresse == null) {
            return null;
        }
        return AdresseDTO.builder()
                .adresseComplete(adresse.getAdresseComplete())
                .quartier(adresse.getQuartier())
                .ville(adresse.getVille())
                .codePostale(adresse.getCodePostale())
                .typeAdresse(adresse.getTypeAdresse())
                .latitude(adresse.getLatitude())
                .longitude(adresse.getLongitude())
                .build();
    }

    public static Adresse toEntity(AdresseDTO dto) {
        if (dto == null) {
            return null;
        }
        Adresse adresse = new Adresse();
        adresse.setAdresseComplete(dto.getAdresseComplete());
        adresse.setQuartier(dto.getQuartier());
        adresse.setVille(dto.getVille());
        adresse.setCodePostale(dto.getCodePostale());
        adresse.setTypeAdresse(dto.getTypeAdresse());
        adresse.setLatitude(dto.getLatitude());
        adresse.setLongitude(dto.getLongitude());
        return adresse;
    }
}