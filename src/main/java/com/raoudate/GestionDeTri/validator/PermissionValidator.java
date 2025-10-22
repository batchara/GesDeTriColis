package com.raoudate.GestionDeTri.validator;

import java.util.ArrayList;
import java.util.List;

public class PermissionValidator {

	public static List<String> validatePermission(String permission) {
		List<String> errors = new ArrayList<>();
		if (permission == null || permission.trim().isEmpty()) {
			errors.add("Veuillez renseigner la permission");
		}
		return errors;
	}
}
