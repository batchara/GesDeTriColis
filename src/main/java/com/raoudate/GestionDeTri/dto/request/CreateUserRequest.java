package com.raoudate.GestionDeTri.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String numTel;
    private LocalDate dateNaissance;
    private String role; 
    private Boolean enabled;
    private Boolean accountLocked;
}
