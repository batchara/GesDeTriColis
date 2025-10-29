package com.raoudate.GestionDeTri.auth;

import com.raoudate.GestionDeTri.email.EmailTemplateName;
import com.raoudate.GestionDeTri.email.EmailsService;
import com.raoudate.GestionDeTri.handler.InvalidTokenException;
import com.raoudate.GestionDeTri.handler.TokenExpiredException;
// role entity not needed here
import com.raoudate.GestionDeTri.repository.RoleRepository;
import com.raoudate.GestionDeTri.repository.TokenRepository;
import com.raoudate.GestionDeTri.repository.UserRepository;
import com.raoudate.GestionDeTri.security.JwtService;
import com.raoudate.GestionDeTri.model.Token;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;

import com.raoudate.GestionDeTri.model.User;
import io.jsonwebtoken.Claims;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor

public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private  final TokenRepository tokenRepository;
    private final EmailsService emailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public String register( RegistrationRequest request) throws MessagingException {
        // Si aucun rôle n'est spécifié, utiliser OPERATEUR par défaut (inscription publique)
        // Si un rôle est spécifié, l'utiliser (création par admin)
        String requestedRoleName = request.getRole() != null 
            ? "ROLE_" + request.getRole().name() 
            : "ROLE_OPERATEUR";
            
        var userRole = roleRepository.findByName(requestedRoleName)
                .orElseThrow(() -> new IllegalStateException(
                    requestedRoleName + " was not initialized. Please check role initialization."
                ));

        var user = User.builder()
                .prenom(request.getFirstname())
                .nom(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .build();

        // add found Role entity to user's roles collection so DB relationship is established
        user.getRoles().add(userRole);

        userRepository.saveAndFlush(user);

        // send validation email and return the generated activation token
        return sendValidationEmail(user, null);

    }
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        String sendValidationEmail(User user , Token oldToken) throws MessagingException {

                if (oldToken != null) {
                        tokenRepository.delete(oldToken);
                }
                var newToken = generateAndeSaveActivationToken(user) ;

                emailsService.sendEmail(
                                user.getEmail(),
                                user.nomComplet(),
                                EmailTemplateName.ACTIVATE_ACCOUNT,
                                activationUrl,
                                newToken,
                                "Account activation"

                );

                return newToken;

        }

    private String generateAndeSaveActivationToken(User user) {
        //generation de Token
        String generateToken = generateActivationToken(6);
        var token = Token.builder()
                .token(generateToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);

        return generateToken;
    }

    private String generateActivationToken(int length) {
        String Characters = "0123456789";

        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(Characters.length());
            codeBuilder.append(Characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }

    public AuthenticationResponse authenticate( AuthenticationRequest request) {

        var auth= authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var claims = new HashMap<String, Object>();
        var user = ((User) auth.getPrincipal());
        
        // Vérifier si le compte est activé
        if (!user.isEnabled()) {
            throw new IllegalStateException("Votre compte n'est pas activé. Veuillez vérifier votre email pour activer votre compte.");
        }
        
        // Vérifier si le compte est bloqué
        if (!user.isAccountNonLocked()) {
            throw new IllegalStateException("Votre compte est bloqué. Veuillez contacter l'administrateur pour plus d'informations.");
        }
        
        claims.put("fulName", user.getEmail());
        var jwtToken = jwtService.generateToken(claims, user);
                // persist the generated JWT so we can revoke it on logout
                try {
                        Date expiration = jwtService.extractClaim(jwtToken, Claims::getExpiration);
                        var tokenEntity = Token.builder()
                                        .token(jwtToken)
                                        .createdAt(LocalDateTime.now())
                                        .expiresAt(expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                                        .user(user)
                                        .build();
                        tokenRepository.save(tokenEntity);
                } catch (Exception ignored) {
                        // if persisting token fails for any reason, continue returning token
                }
        return AuthenticationResponse.builder()
                .token(jwtToken).build();
    }

    @Transactional(noRollbackFor = TokenExpiredException.class)
    public void activateAcount(String email, String token) throws MessagingException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Token saveToken = tokenRepository.findByToken(token)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new InvalidTokenException("Invalid Token"));

        if (LocalDateTime.now().isAfter(saveToken.getExpiresAt())) {
                sendValidationEmail(user, saveToken);
                throw new TokenExpiredException("Token expired. A new token has been sent to the same email");
        }

        user.setEnabled(true);
        userRepository.save(user);
        saveToken.setValidateAt(LocalDateTime.now());
        tokenRepository.save(saveToken);
        tokenRepository.delete(saveToken);

        sendActivationConfirmationEmail(user);

    }

    private void sendActivationConfirmationEmail(User user) throws MessagingException {
        String loginUrl = "http://localhost:8080/login";


            emailsService.sendEmail(
                    user.getEmail(),
                    user.nomComplet(),
                    EmailTemplateName.CONFIRM_ACCOUNT,
                    loginUrl,
                    null,
                    "Your account has been activated!"
            );

    }

    public void resendActivation(String email) throws MessagingException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec cet email"));

        if (user.isEnabled()) {
            throw new IllegalStateException("Le compte est déjà activé");
        }

        // Récupérer l'ancien token s'il existe
        Token oldToken = tokenRepository.findTopByUserOrderByCreatedAtDesc(user).orElse(null);

        // Générer et envoyer un nouveau token
        sendValidationEmail(user, oldToken);
    }
}
