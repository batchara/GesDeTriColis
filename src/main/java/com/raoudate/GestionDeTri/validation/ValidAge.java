package com.raoudate.GestionDeTri.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgeValidator.class)
@Documented
public @interface ValidAge {

    String message() default "L'âge doit être d'au moins {min} ans";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int min() default 18;

    int max() default 120;
}
