package com.raoudate.GestionDeTri.Enum;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;

import static com.raoudate.GestionDeTri.Enum.Permission.*;

public enum RoleType {
    USER(Collections.emptySet()),

    ADMIN(
            Set.of(
                    ADMIN_READ,
                    ADMIN_UPDATE,
                    ADMIN_CREATE,
                    ADMIN_DELETE,

                    SUPERVISEUR_READ,
                    SUPERVISEUR_UPDATE,
                    SUPERVISEUR_CREATE,
                    SUPERVISEUR_DELETE,

                    OPERATEUR_READ,
                    OPERATEUR_UPDATE,
                    OPERATEUR_CREATE,
                    OPERATEUR_DELETE
            )
    ),

    OPERATEUR(
            Set.of(
                    OPERATEUR_READ,
                    OPERATEUR_UPDATE

            )
    ),
    SUPERVISEUR(
            Set.of  (SUPERVISEUR_READ,
                    SUPERVISEUR_UPDATE,
                    SUPERVISEUR_CREATE,
                    SUPERVISEUR_DELETE)
    );

    @Getter
    private final Set <Permission> permission;

    RoleType(Set<Permission> permission) {
        this.permission = permission;
    }
}
