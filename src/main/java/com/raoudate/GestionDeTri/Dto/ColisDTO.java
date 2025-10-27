package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.Enum.StatutColis;
import com.raoudate.GestionDeTri.model.*;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ColisDTO {

    private Integer id;
    private String codeSuivi;
    private BigDecimal poids;
    private String nomExp;
    private String nomDest;
    private String telDest;
    private String adresseDest; // Adresse du destinataire
    private Instant dateEnvoi;
    private Instant datePrevue;
    private StatutColis statut;
    private CentreDeTriDTO centreTri;
    private AgenceDTO agenceAffectee;

    public static ColisDTO fromEntity(Colis colis) {
        if (colis == null) {
            return null;
        }
        return ColisDTO.builder()
                .id(colis.getId())
                .codeSuivi(colis.getCodeSuivi())
                .poids(colis.getPoids())
                .nomExp(colis.getNomExp())
                .nomDest(colis.getNomDest())
                .telDest(colis.getTelDest())
                .adresseDest(colis.getAdresseDest())
                .dateEnvoi(colis.getDateEnvoi())
                .datePrevue(colis.getDatePrevue())
                .statut(colis.getStatut())
                .centreTri(CentreDeTriDTO.fromEntity(colis.getCentreTri()))
                .agenceAffectee(AgenceDTO.fromEntity(colis.getAgenceAffectee()))
                .build();
    }

    public static Colis toEntity(ColisDTO dto) {
        if (dto == null) {
            return null;
        }
        Colis colis = new Colis();
        colis.setId(dto.getId());
        colis.setCodeSuivi(dto.getCodeSuivi());
        colis.setPoids(dto.getPoids());
        colis.setNomExp(dto.getNomExp());
        colis.setNomDest(dto.getNomDest());
        colis.setTelDest(dto.getTelDest());
        colis.setAdresseDest(dto.getAdresseDest());
        colis.setDateEnvoi(dto.getDateEnvoi());
        colis.setDatePrevue(dto.getDatePrevue());
        colis.setStatut(dto.getStatut());
        colis.setCentreTri(CentreDeTriDTO.toEntity(dto.getCentreTri()));
        colis.setAgenceAffectee(AgenceDTO.toEntity(dto.getAgenceAffectee()));
        return colis;
    }
}