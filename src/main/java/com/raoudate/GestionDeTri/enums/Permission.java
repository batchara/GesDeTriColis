package com.raoudate.GestionDeTri.enums;

import lombok.Getter;

@Getter
public enum Permission {

    // ADMIN permissions
    ADMIN_READ("admin:read"),
    ADMIN_CREATE("admin:create"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete"),
    ADMIN_VIEW_DASHBOARD("admin:view_dashboard"),

    // SUPERVISEUR permissions
    SUPERVISEUR_READ("superviseur:read"),
    SUPERVISEUR_CREATE("superviseur:create"),
    SUPERVISEUR_UPDATE("superviseur:update"),
    SUPERVISEUR_DELETE("superviseur:delete"),
    SUPERVISEUR_VIEW_DASHBOARD("superviseur:view_dashboard");

    @Getter
    private final String description;
    Permission(String description) {
        this.description = description;
    }

}
