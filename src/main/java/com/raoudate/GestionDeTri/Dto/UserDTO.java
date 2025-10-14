package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class UserDTO {
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String numTel;
    private List<String> roles;

    // Conversion de l'entité vers le DTO
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
        .numTel(user.getNumTel())
        .roles(user.getRoles().stream()
            .map(role -> role.getName()) // Extraire juste les noms des rôles
            .collect(Collectors.toList()))
                .build();
    }

    // Conversion de DTO vers l'entité
    public static User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        User user = new User() {
            // On définit une classe anonyme si Utilisateur est abstract
        };
        user.setId(userDTO.getId());
        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setEmail(userDTO.getEmail());
    user.setNumTel(userDTO.getNumTel());
        // Note : Les rôles doivent être récupérés via la base de données.
        return user;
    }
}