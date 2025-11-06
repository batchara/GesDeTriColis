package com.raoudate.GestionDeTri.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FirstLoginPasswordChangeRequest {
    
    @Email(message = "Email invalide")
    @NotBlank(message = "Email requis")
    private String email;
    
    @NotBlank(message = "Mot de passe temporaire requis")
    private String temporaryPassword;
    
    @NotBlank(message = "Nouveau mot de passe requis")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial (@#$%^&+=!)"
    )
    private String newPassword;
    
    @NotBlank(message = "Confirmation du mot de passe requise")
    private String confirmPassword;
}
