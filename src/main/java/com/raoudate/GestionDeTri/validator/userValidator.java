package com.raoudate.GestionDeTri.validator;

import com.raoudate.GestionDeTri.Dto.UserDTO;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class userValidator {

    public static List<String> validateUser(UserDTO dto) {
        List<String> errors = new ArrayList<>();
        if (dto == null) {
            errors.add("Veuillez renseigner l'utilisateur");
            return errors;
        }

        if (!StringUtils.hasText(dto.getNom())) {
            errors.add("Veuillez renseigner le nom");
        }

        if (!StringUtils.hasText(dto.getPrenom())) {
            errors.add("Veuillez renseigner le prénom");
        }

        if (!StringUtils.hasText(dto.getEmail())) {
            errors.add("Veuillez renseigner l'email");
        } else if (!dto.getEmail().contains("@")) {
            errors.add("Email invalide");
        }

        if (!StringUtils.hasText(dto.getNumTel())) {
            errors.add("Veuillez renseigner le numéro de téléphone");
        }

        return errors;
    }
}

