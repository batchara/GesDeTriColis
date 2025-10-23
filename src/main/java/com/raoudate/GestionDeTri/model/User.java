package com.raoudate.GestionDeTri.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.raoudate.GestionDeTri.Enum.RoleType;
import jakarta.persistence.*;
import lombok.*;
// audit fields are handled in AbstractEntity
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "utilisateurs")
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractEntity implements UserDetails, Principal {

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private boolean accountLocked = false;

    @Column(name = "num_tel", length = 32)
    private String numTel;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Token> tokens;


    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "id_user"),
        inverseJoinColumns = @JoinColumn(name = "id_role")
    )
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();


 
    public String nomComplet() {
        
        String base = String.join(" ",
                Optional.ofNullable(prenom).orElse(""),
                Optional.ofNullable(nom).orElse("")
        ).trim();
        return base.isEmpty() ? email : base;
    }        

    @Override
    public String getName() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Utiliser les permissions de la base de données plutôt que celles de l'enum
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        System.out.println("🔍 [getAuthorities] User: " + email);
        System.out.println("🔍 [getAuthorities] Nombre de rôles: " + (roles != null ? roles.size() : 0));
        
        // Ajouter les permissions depuis les rôles de la base de données
        if (roles != null) {
            for (Role dbRole : roles) {
                // Ajouter le rôle lui-même
                System.out.println("🔍 [getAuthorities] Rôle: " + dbRole.getName());
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(dbRole.getName()));
                
                // Ajouter toutes les permissions du rôle
                if (dbRole.getPermissions() != null) {
                    System.out.println("🔍 [getAuthorities] Nombre de permissions: " + dbRole.getPermissions().size());
                    for (Permissions permission : dbRole.getPermissions()) {
                        System.out.println("🔍 [getAuthorities] Permission: " + permission.getNom().name());
                        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(permission.getNom().name()));
                    }
                }
            }
        }
        
        // Si aucun rôle en base, fallback sur l'enum (pour compatibilité)
        if (authorities.isEmpty() && role != null) {
            System.out.println("⚠️ [getAuthorities] Fallback sur l'enum role: " + role);
            return role.getAuthorities();
        }
        
        System.out.println("✅ [getAuthorities] Total authorities: " + authorities.size());
        authorities.forEach(auth -> System.out.println("   - " + auth.getAuthority()));
        
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !accountLocked; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public String getPassword() {
        return password;
    }
}

