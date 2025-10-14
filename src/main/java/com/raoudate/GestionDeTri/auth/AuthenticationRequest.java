package com.raoudate.GestionDeTri.auth;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class AuthenticationRequest {
    @NotEmpty(message = "password is required")
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial (@#$%^&+=!)"
    )
    private String password;

    @Email(message = "email is not valid --> raoudate@mail")
    @NotEmpty(message = "email is required")
    @NotBlank(message = "email is required")
    private String email;
}
