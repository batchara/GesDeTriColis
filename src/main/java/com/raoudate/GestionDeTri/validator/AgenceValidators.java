package com.raoudate.GestionDeTri.validator;

import com.raoudate.GestionDeTri.dto.response.AgenceDTO;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AgenceValidators {

    public static List<String> validateAgence(AgenceDTO agenceDTO) {
        List<String> errors = new ArrayList<>();
        if (agenceDTO == null) {
            errors.add("Veuillez renseigner l'agence");
            return errors;
        }
        if (!StringUtils.hasText(agenceDTO.getCode())) {
            errors.add("Veuillez renseigner le code de l'agence");
        }
        if (!StringUtils.hasText(agenceDTO.getNom())) {
            errors.add("Veuillez renseigner le nom de l'agence");
        }
        if (!StringUtils.hasText(agenceDTO.getAdresseComplete())) {
            errors.add("Veuillez renseigner l'adresse complète de l'agence");
        }

          if(!StringUtils.hasText(agenceDTO.getRegion())) {
            errors.add("Veuillez renseigner la région");
        }

         if(!StringUtils.hasText(agenceDTO.getTel())) {
            errors.add("Veuillez renseigner le numéro de téléphone");
        }   
        return errors;
    }
}
