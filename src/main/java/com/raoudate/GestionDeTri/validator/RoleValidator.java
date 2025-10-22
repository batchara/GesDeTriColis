package com.raoudate.GestionDeTri.validator;

import java.util.ArrayList;
import java.util.List;

public class RoleValidator {

	public static List<String> validateRole(String roleName) {
		List<String> errors = new ArrayList<>();
		if (roleName == null || roleName.trim().isEmpty()) {
			errors.add("Veuillez renseigner le nom du rôle");
		}
		return errors;
	}
}
