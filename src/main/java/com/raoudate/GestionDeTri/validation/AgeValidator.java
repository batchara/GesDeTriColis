package com.raoudate.GestionDeTri.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<ValidAge, LocalDate> {

    private int minAge;
    private int maxAge;

    @Override
    public void initialize(ValidAge constraintAnnotation) {
        this.minAge = constraintAnnotation.min();
        this.maxAge = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(LocalDate dateNaissance, ConstraintValidatorContext context) {
        if (dateNaissance == null) {
            return true; // @NotNull s'occupe de la validation null
        }

        LocalDate today = LocalDate.now();
        Period period = Period.between(dateNaissance, today);
        int age = period.getYears();

        return age >= minAge && age <= maxAge;
    }
}
