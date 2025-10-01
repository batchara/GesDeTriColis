package com.raoudate.GestionDeTri.model;

import jakarta.persistence.*; // Import des annotations JPA
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(callSuper = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roles extends AbstractEntity {

    @Column(name = "nonm")
    private String nom;   // Nom du rôle (ex. ADMIN, OPERATEUR, SUPERVISEUR)

    @Column(name = "nom")
    private String label;

    // Relation ManyToMany avec la classe Permissions
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions", // Table d'association
            joinColumns = @JoinColumn(name = "role_id"), // ID de la table rôle
            inverseJoinColumns = @JoinColumn(name = "permission_id") // ID de la table permission
    )
    @Builder.Default
    private Set<Permissions> permissions = new HashSet<>();

}
