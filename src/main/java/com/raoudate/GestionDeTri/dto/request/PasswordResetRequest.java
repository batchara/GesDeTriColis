package com.raoudate.GestionDeTri.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class PasswordResetRequest {
    private String email;
}
