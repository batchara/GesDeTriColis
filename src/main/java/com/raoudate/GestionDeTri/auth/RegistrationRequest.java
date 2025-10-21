package com.raoudate.GestionDeTri.auth;

import com.raoudate.GestionDeTri.Enum.RoleType;
import com.raoudate.GestionDeTri.validation.ValidAge;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder

public class RegistrationRequest {

    @NotEmpty(message = "Le prénom est obligatoire")
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstname;

    @NotEmpty(message = "Le nom est obligatoire")
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastname;

    @NotEmpty(message = "Le mot de passe est obligatoire")
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial (@#$%^&+=!)"
    )
    private String password;

    @Email(message = "L'email n'est pas valide")
    @NotEmpty(message = "L'email est obligatoire")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @ValidAge(min = 18, message = "Vous devez avoir au moins 18 ans pour vous inscrire")
    private LocalDate dateNaissance;

    @Pattern(
            regexp = "^(\\+228)?[0-9]{8,10}$",
            message = "Le numéro de téléphone doit être au format togolais (+228 XX XX XX XX ou 8 chiffres)"
    )
    private String numTel;

    private RoleType role;

}
