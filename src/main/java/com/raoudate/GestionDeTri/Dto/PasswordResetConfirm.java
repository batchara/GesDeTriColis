package com.raoudate.GestionDeTri.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 🔐 DTO pour confirmer la réinitialisation de mot de passe avec le token
 */
@Getter
@Setter
@Builder
public class PasswordResetConfirm {
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le code de vérification est obligatoire")
    @Size(min = 6, max = 6, message = "Le code doit contenir exactement 6 chiffres")
    private String token;
    
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String newPassword;
}
