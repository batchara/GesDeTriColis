package com.raoudate.GestionDeTri.auth;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class AuthenticationRequest {
    @NotEmpty(message = "password is required")
    @NotBlank(message = "password is required")
    private String password;

    @Email(message = "email is not valid --> raoudate@mail")
    @NotEmpty(message = "email is required")
    @NotBlank(message = "email is required")
    private String email;
}
