package com.raoudate.GestionDeTri.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<MessageResponse> register(
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        authenticationService.register(request);
        return ResponseEntity.accepted().body(
                new MessageResponse("User registered successfully. Please check your email to activate your account.")
        );
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) {
        log.info("🔐 Tentative de connexion pour: {}", request.getEmail());
        AuthenticationResponse response = authenticationService.authenticate(request);
        log.info("✅ Connexion réussie pour: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activate-account")
    public ResponseEntity<MessageResponse> activateAccount(
            @RequestParam String email,
            @RequestParam String token
    ) throws MessagingException {
        authenticationService.activateAcount(email, token);
        return ResponseEntity.ok(
                new MessageResponse("Account activated successfully. You can now login.")
        );
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<MessageResponse> resendActivationEmail(
            @RequestParam String email
    ) throws MessagingException {
        authenticationService.resendActivation(email);
        return ResponseEntity.ok(
                new MessageResponse("Activation email resent successfully. Please check your inbox.")
        );
    }

    @PostMapping("/change-temporary-password")
    public ResponseEntity<AuthenticationResponse> changeTemporaryPassword(
            @RequestBody @Valid FirstLoginPasswordChangeRequest request
    ) {
        return ResponseEntity.ok(authenticationService.changeTemporaryPassword(request));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<MessageResponse> requestPasswordReset(
            @RequestParam String email
    ) throws MessagingException {
        authenticationService.requestPasswordReset(email);
        return ResponseEntity.ok(
                new MessageResponse("Password reset email sent successfully. Please check your inbox.")
        );
    }

    @PostMapping("/confirm-password-reset")
    public ResponseEntity<MessageResponse> confirmPasswordReset(
            @RequestParam String email,
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        authenticationService.confirmPasswordReset(email, token, newPassword);
        return ResponseEntity.ok(
                new MessageResponse("Password reset successfully. You can now login with your new password.")
        );
    }
}
