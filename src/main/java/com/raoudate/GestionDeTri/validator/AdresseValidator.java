package com.raoudate.GestionDeTri.validator;

import com.raoudate.GestionDeTri.Dto.AdresseDTO;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AdresseValidator {

	public static List<String> validateAdresse(AdresseDTO dto) {
		List<String> errors = new ArrayList<>();
		if (dto == null) {
			errors.add("Veuillez renseigner l'adresse");
			return errors;
		}

		if (!StringUtils.hasText(dto.getAdresseComplete())) {
			errors.add("Veuillez renseigner l'adresse complète");
		}

		

		if (!StringUtils.hasText(dto.getRue())) {
			errors.add("Veuillez renseigner la rue");
		}

    
		return errors;
	}
}
