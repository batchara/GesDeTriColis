package com.raoudate.GestionDeTri.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 🔐 DTO pour la demande de réinitialisation de mot de passe
 */
@Getter
@Setter
@Builder
public class PasswordResetRequest {
    private String email;
}
