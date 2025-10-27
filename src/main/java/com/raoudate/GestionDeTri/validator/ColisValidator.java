package com.raoudate.GestionDeTri.validator;

import com.raoudate.GestionDeTri.Dto.ColisDTO;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ColisValidator {

	public static List<String> validateColis(ColisDTO dto) {
		List<String> errors = new ArrayList<>();
		if (dto == null) {
			errors.add("Veuillez renseigner le colis");
			return errors;
		}

		if (!StringUtils.hasText(dto.getCodeSuivi())) {
			errors.add("Veuillez renseigner le code de suivi");
		}

		BigDecimal poids = dto.getPoids();
		if (poids == null || poids.signum() <= 0) {
			errors.add("Veuillez renseigner un poids valide (> 0)");
		}

		if (!StringUtils.hasText(dto.getNomExp())) {
			errors.add("Veuillez renseigner le nom de l'expéditeur");
		}

		if (!StringUtils.hasText(dto.getNomDest())) {
			errors.add("Veuillez renseigner le nom du destinataire");
		}

		if (!StringUtils.hasText(dto.getTelDest())) {
			errors.add("Veuillez renseigner le téléphone du destinataire");
		}

		if (!StringUtils.hasText(dto.getAdresseDest())) {
			errors.add("Veuillez renseigner l'adresse de destination");
		}

		return errors;
	}
}
