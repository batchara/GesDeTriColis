package com.raoudate.GestionDeTri.auth;

import com.raoudate.GestionDeTri.audit.AuditLogService;
import com.raoudate.GestionDeTri.email.EmailTemplateName;
import com.raoudate.GestionDeTri.email.EmailsService;
import com.raoudate.GestionDeTri.exception.InvalidTokenException;
import com.raoudate.GestionDeTri.exception.TokenExpiredException;
import com.raoudate.GestionDeTri.services.LoginAttemptService;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.time.LocalDate;
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
    private final AuditLogService auditLogService;
    private final LoginAttemptService loginAttemptService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    @Transactional
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


        try {
            return sendValidationEmailWithPassword(user, null, request.getPassword());
        } catch (MessagingException e) {
            // Log l'erreur pour debugging
            System.err.println(" Échec de l'envoi de l'email d'activation à : " + user.getEmail());
            System.err.println(" Erreur : " + e.getMessage());
            // Propager l'exception pour annuler la transaction
            throw new MessagingException("Impossible d'envoyer l'email d'activation. Veuillez vérifier que l'adresse email est valide et fonctionnelle.", e);
        }

    }
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        String sendValidationEmailWithPassword(User user, Token oldToken, String rawPassword) throws MessagingException {

                if (oldToken != null) {
                        tokenRepository.delete(oldToken);
                }
                
                var newToken = generateAndeSaveActivationToken(user, rawPassword) ;

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
        
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        String sendValidationEmail(User user , Token oldToken) throws MessagingException {

                if (oldToken != null) {
                        tokenRepository.delete(oldToken);
                }
                
                // Récupérer le mot de passe temporaire du token existant ou utiliser celui de l'utilisateur
                String temporaryPassword = (oldToken != null && oldToken.getTemporaryPassword() != null) 
                    ? oldToken.getTemporaryPassword() 
                    : null;
                
                var newToken = generateAndeSaveActivationToken(user, temporaryPassword) ;

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

    private String generateAndeSaveActivationToken(User user, String temporaryPassword) {
        //generation de Token
        String generateToken = generateActivationToken(6);
        var token = Token.builder()
                .token(generateToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .temporaryPassword(temporaryPassword)
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
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();
        
        // ÉTAPE 1: Vérifier si le compte existe et s'il est verrouillé AVANT d'authentifier
        var existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent() && existingUser.get().isAccountLocked()) {
            auditLogService.logAuthentication(
                request.getEmail(),
                "FAILED",
                "Tentative de connexion - Compte bloqué après 3 tentatives échouées",
                ipAddress,
                userAgent
            );
            throw new IllegalStateException(" Votre compte est bloqué après 3 tentatives de connexion échouées. Utilisez 'Mot de passe oublié' pour le débloquer ou contactez l'administrateur.");
        }
        
        try {
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
                auditLogService.logAuthentication(
                    request.getEmail(),
                    "FAILED",
                    "Tentative de connexion - Compte non activé",
                    ipAddress,
                    userAgent
                );
                throw new IllegalStateException("Votre compte n'est pas activé. Veuillez vérifier votre email pour activer votre compte.");
            }
            
            // Connexion réussie - Réinitialiser le compteur
            loginAttemptService.loginSucceeded(request.getEmail());
            
            // Mettre à jour la date de dernière connexion
            user.setLastLogin(LocalDate.now());
            userRepository.save(user);
            
            // Vérifier si l'utilisateur doit changer son mot de passe
            boolean mustChangePassword = user.isMustChangePassword();
            
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
            
            // Logger la connexion réussie
            if (mustChangePassword) {
                auditLogService.logAuthentication(
                    request.getEmail(),
                    "SUCCESS",
                    "Connexion réussie - Changement de mot de passe requis",
                    ipAddress,
                    userAgent
                );
            } else {
                auditLogService.logAuthentication(
                    request.getEmail(),
                    "SUCCESS",
                    "Connexion réussie",
                    ipAddress,
                    userAgent
                );
            }
            
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .mustChangePassword(mustChangePassword)
                    .build();
        } catch (BadCredentialsException e) {
            // Tentative échouée - Incrémenter le compteur
            loginAttemptService.loginFailed(request.getEmail());
            int remainingAttempts = loginAttemptService.getRemainingAttempts(request.getEmail());
            
            // Logger l'échec de connexion avec le nombre de tentatives restantes
            auditLogService.logAuthentication(
                request.getEmail(),
                "FAILED",
                "Identifiants invalides - Tentatives restantes: " + remainingAttempts,
                ipAddress,
                userAgent
            );
            
            if (remainingAttempts == 0) {
                throw new BadCredentialsException(" Compte bloqué après 3 tentatives échouées. Contactez l'administrateur.");
            } else {
                throw new BadCredentialsException(" Identifiants invalides. Tentatives restantes: " + remainingAttempts);
            }
        }
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

        // Récupérer le mot de passe temporaire avant de supprimer le token
        String temporaryPassword = saveToken.getTemporaryPassword();
        
        user.setEnabled(true);
        userRepository.save(user);
        saveToken.setValidateAt(LocalDateTime.now());
        tokenRepository.save(saveToken);
        tokenRepository.delete(saveToken);

        // Envoyer l'email de confirmation avec le mot de passe temporaire
        sendActivationConfirmationEmailWithPassword(user, temporaryPassword);

    }

    
    private void sendActivationConfirmationEmailWithPassword(User user, String temporaryPassword) throws MessagingException {
        String loginUrl = "http://localhost:4200/login";

        // Envoyer l'email de confirmation avec le mot de passe temporaire
        emailsService.sendEmail(
                user.getEmail(),
                user.nomComplet(),
                EmailTemplateName.CONFIRM_ACCOUNT,
                loginUrl,
                temporaryPassword, // Envoyer le mot de passe temporaire
                "Votre compte a été activé - Mot de passe temporaire"
        );
        
        System.out.println(" Email de confirmation envoyé avec mot de passe temporaire à: " + user.getEmail());
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
    
   
    @Transactional
    public AuthenticationResponse changeTemporaryPassword(FirstLoginPasswordChangeRequest request) {
        // Vérifier que les mots de passe correspondent
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("Les mots de passe ne correspondent pas");
        }
        
        // Authentifier avec le mot de passe temporaire
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getTemporaryPassword()
                )
            );
        } catch (Exception e) {
            throw new IllegalStateException("Mot de passe temporaire invalide");
        }
        
        // Récupérer l'utilisateur
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        // Vérifier que le compte est activé
        if (!user.isEnabled()) {
            throw new IllegalStateException("Votre compte n'est pas activé");
        }
        
        // Changer le mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false); // Retirer le flag de changement forcé
        userRepository.save(user);
        
        // Générer un token JWT
        var claims = new HashMap<String, Object>();
        claims.put("fulName", user.getEmail());
        var jwtToken = jwtService.generateToken(claims, user);
        
        // Sauvegarder le token
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
        
        System.out.println(" Mot de passe changé avec succès pour: " + user.getEmail());
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
    
    
    @Transactional
    public void requestPasswordReset(String email) throws MessagingException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        // Générer un code de réinitialisation
        String resetCode = generateActivationToken(6);
        
        // Supprimer les anciens tokens de réinitialisation
        tokenRepository.findAllByUserId(user.getId()).forEach(token -> {
            if (token.getToken().length() == 6) { // Code de 6 chiffres = token de réinitialisation
                tokenRepository.delete(token);
            }
        });
        
        // Créer un nouveau token (valide 30 minutes)
        var token = Token.builder()
                .token(resetCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .user(user)
                .build();
        tokenRepository.save(token);
        
        // Envoyer l'email
        emailsService.sendEmail(
                user.getEmail(),
                user.nomComplet(),
                EmailTemplateName.RESET_PASSWORD,
                null, // Pas besoin d'URL
                resetCode,
                "Réinitialisation de mot de passe"
        );
        
        System.out.println(" Code de réinitialisation envoyé à: " + email);
    }
    
   
    @Transactional
    public void confirmPasswordReset(String email, String token, String newPassword) {
        System.out.println(" [RESET PASSWORD] Email reçu: " + email);
        System.out.println(" [RESET PASSWORD] Code reçu: '" + token + "' (longueur: " + token.length() + ")");
        
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        System.out.println(" [RESET PASSWORD] Utilisateur trouvé: " + user.getEmail() + " (ID: " + user.getId() + ")");
        
        var resetToken = tokenRepository.findByToken(token)
                .filter(t -> {
                    System.out.println(" [RESET PASSWORD] Token trouvé en BDD: '" + t.getToken() + "' pour user ID: " + t.getUser().getId());
                    boolean matches = t.getUser().getId().equals(user.getId());
                    System.out.println(" [RESET PASSWORD] User ID correspond: " + matches);
                    return matches;
                })
                .orElseThrow(() -> {
                    System.out.println(" [RESET PASSWORD] Token non trouvé ou user ID ne correspond pas");
                    return new InvalidTokenException("Code invalide");
                });
        
        System.out.println(" [RESET PASSWORD] Token valide, expiration: " + resetToken.getExpiresAt());
        
        if (LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
            System.out.println(" [RESET PASSWORD] Token expiré");
            throw new TokenExpiredException("Code expiré. Veuillez demander un nouveau code.");
        }
        
        // Changer le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Déverrouiller le compte si verrouillé
        if (user.isAccountLocked()) {
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setLockTime(null);
            user.setLastFailedLogin(null);
        }
        
        userRepository.save(user);
        tokenRepository.delete(resetToken);
        
        System.out.println(" Mot de passe réinitialisé pour: " + email);
    }
    
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) 
                RequestContextHolder.currentRequestAttributes();
            jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
            
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) 
                RequestContextHolder.currentRequestAttributes();
            jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
