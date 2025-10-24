package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.Enum.TypeAdresse;
import com.raoudate.GestionDeTri.model.Adresse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdresseDTO {

    private Integer id;
    private String adresseComplete;
    private String rue;
    private String quartier; // Nouveau champ
    private String ville; // Nouveau champ
    private String codePostale; // Nouveau champ
    private TypeAdresse typeAdresse;
    private Double latitude;
    private Double longitude;


    public static AdresseDTO fromEntity(Adresse adresse) {
        if (adresse == null) {
            return null;
        }
        return AdresseDTO.builder()
                .id(adresse.getId())
                .adresseComplete(adresse.getAdresseComplete())
                .rue(adresse.getRue())
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
        adresse.setRue(dto.getRue());
        adresse.setTypeAdresse(dto.getTypeAdresse());
        adresse.setLatitude(dto.getLatitude());
        adresse.setLongitude(dto.getLongitude());
        return adresse;
    }
}
