package com.raoudate.GestionDeTri.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name ="Authentification")
public class AuthentificationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<MessageResponse> register(

            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        service.register(request);
        return ResponseEntity.accepted().body(new MessageResponse("Inscription réussie ! Vérifiez votre email pour activer votre compte."));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<MessageResponse> authenticate(@RequestBody @Valid AuthenticationRequest request){
        AuthenticationResponse response = service.authenticate(request);
        return ResponseEntity.ok(new MessageResponse("Authentification réussie ! Token : " + response.getToken()));
    }

    @GetMapping("/activate-account")
    public ResponseEntity<MessageResponse> confirm(
            @RequestParam String email,
            @RequestParam String token) throws MessagingException {
        service.activateAcount(email,token);
        return ResponseEntity.ok(new MessageResponse("Votre compte a été activé avec succès !"));
    }


}
