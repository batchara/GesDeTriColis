package com.raoudate.GestionDeTri.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {

    ACTIVATE_ACCOUNT("activate_account"),
    CONFIRM_ACCOUNT("confirm_account")

    ;
    private final String name;

    EmailTemplateName(String name) {

        this.name = name;
    }


}
