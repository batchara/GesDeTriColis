package com.raoudate.GestionDeTri.validator;

import com.raoudate.GestionDeTri.dto.response.CentreDeTriDTO;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CentreValidator {

	public static List<String> validateCentre(CentreDeTriDTO dto) {
		List<String> errors = new ArrayList<>();
		if (dto == null) {
			errors.add("Veuillez renseigner le centre");
			return errors;
		}

		if (!StringUtils.hasText(dto.getNom())) {
			errors.add("Veuillez renseigner le nom du centre");
		}

		if (!StringUtils.hasText(dto.getTelephone())) {
			errors.add("Veuillez renseigner le téléphone du centre");
		}

		if (!StringUtils.hasText(dto.getAdresseCentre())) {
			errors.add("Veuillez renseigner l'adresse du centre");
		}

		return errors;
	}
}
