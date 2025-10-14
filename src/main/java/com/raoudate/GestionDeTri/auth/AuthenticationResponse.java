package com.raoudate.GestionDeTri.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class AuthenticationResponse {

    private final String token;
}
