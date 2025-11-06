package com.raoudate.GestionDeTri.auth;

import com.raoudate.GestionDeTri.audit.AuditLogService;
import com.raoudate.GestionDeTri.repository.TokenRepository;
import com.raoudate.GestionDeTri.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LogoutController {

    private final TokenRepository tokenRepository;
    private final AuditLogService auditLogService;
    private final JwtService jwtService;

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        try {
            // Extraire le token du header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                
                // Extraire le username du token
                String username = jwtService.extractUsername(jwt);
                
                // Récupérer l'authentification actuelle
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String currentUsername = auth != null ? auth.getName() : username;
                
                // Supprimer le token de la base de données
                tokenRepository.findByToken(jwt).ifPresent(token -> {
                    tokenRepository.delete(token);
                });
                
                // Logger la déconnexion
                auditLogService.logAuthentication(
                    currentUsername,
                    "SUCCESS",
                    "Déconnexion réussie",
                    getClientIpAddress(request),
                    getUserAgent(request)
                );
                
                // Nettoyer le contexte de sécurité
                SecurityContextHolder.clearContext();
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "Déconnexion réussie");
                return ResponseEntity.ok(response);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Aucun token trouvé");
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            auditLogService.logError(
                "LOGOUT",
                "AUTH",
                "Erreur lors de la déconnexion",
                e.getMessage()
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors de la déconnexion");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
