package com.raoudate.GestionDeTri.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

public enum Permission {

    ADMIN_READ("read"),
    ADMIN_UPDATE("update"),
    ADMIN_CREATE("create"),
    ADMIN_DELETE("delete"),

    SUPERVISEUR_READ("read"),
    SUPERVISEUR_UPDATE("update"),
    SUPERVISEUR_CREATE("create"),
    SUPERVISEUR_DELETE("delete"),

    OPERATEUR_READ("read"),
    OPERATEUR_UPDATE("update"),
    OPERATEUR_CREATE("create"),
    OPERATEUR_DELETE("delete");

    @Getter
    private final String permission;


}
