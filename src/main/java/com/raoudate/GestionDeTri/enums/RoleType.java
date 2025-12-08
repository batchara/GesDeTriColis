package com.raoudate.GestionDeTri.enums;

import lombok.Getter;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.raoudate.GestionDeTri.enums.Permission.*;

@Getter
public enum RoleType {


   

    SUPERVISEUR(
            Set.of(
                    SUPERVISEUR_READ,
                    SUPERVISEUR_CREATE,
                    SUPERVISEUR_UPDATE,
                    SUPERVISEUR_DELETE,
                    SUPERVISEUR_VIEW_DASHBOARD
            )
    ),

    
        ADMIN(
            Set.of(
                    // Permissions admin
                    ADMIN_READ,
                    ADMIN_CREATE,
                    ADMIN_UPDATE,
                    ADMIN_DELETE,
                    ADMIN_VIEW_DASHBOARD,
                    SUPERVISEUR_CREATE,
                    SUPERVISEUR_UPDATE,
                    SUPERVISEUR_DELETE,
                    SUPERVISEUR_VIEW_DASHBOARD

            )
    ),

    // rôle vide (optionnel)
    OPERATEUR(Collections.emptySet());

    private final Set<Permission> permissions;

    RoleType(Set<Permission> permissions) {
        this.permissions = permissions;
    }

        public List<SimpleGrantedAuthority> getAuthorities(){
                // toList() may return an immutable list depending on the JVM implementation
                // so create a mutable ArrayList to allow additions later
                List<SimpleGrantedAuthority> authorities = getPermissions()
                        .stream()
                        .map(permission -> new SimpleGrantedAuthority(permission.name()))
                        .collect(Collectors.toCollection(ArrayList::new));
                authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
                return authorities;
        }
}