package com.raoudate.GestionDeTri.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
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
@SQLRestriction("is_deleted = false")
public class User extends AbstractEntity implements UserDetails, Principal {

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "email", nullable = false)
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
    
    @Column(name = "must_change_password", nullable = false)
    @Builder.Default
    private boolean mustChangePassword = false;
    
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;
    
    @Column(name = "last_failed_login")
    private LocalDate lastFailedLogin;
    
    @Column(name = "lock_time")
    private LocalDate lockTime;
    
    @Column(name = "last_login")
    private LocalDate lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Token> tokens;


    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
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
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if (roles != null) {
            for (Role dbRole : roles) {
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(dbRole.getName()));
                
                if (dbRole.getPermissions() != null) {
                    for (Permissions permission : dbRole.getPermissions()) {
                        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(permission.getNom().name()));
                    }
                }
            }
        }
        
        if (authorities.isEmpty()) {
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_OPERATEUR"));
        }
        
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

