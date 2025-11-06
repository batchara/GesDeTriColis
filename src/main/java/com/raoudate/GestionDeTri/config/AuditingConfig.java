package com.raoudate.GestionDeTri.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuration pour activer l'audit automatique des entités JPA.
 * Cette configuration permet de tracer automatiquement :
 * - Qui a créé une entité (@CreatedBy)
 * - Qui a modifié une entité (@LastModifiedBy)
 * - Quand elle a été créée (@CreatedDate)
 * - Quand elle a été modifiée (@LastModifiedDate)
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    /**
     * Bean qui fournit l'identifiant de l'utilisateur actuel pour l'audit.
     * Utilisé automatiquement par Spring Data JPA pour remplir les champs @CreatedBy et @LastModifiedBy
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new ApplicationAuditorAware();
    }
}

/**
 * Implémentation de AuditorAware pour capturer l'utilisateur connecté.
 * Cette classe est utilisée par Spring Data JPA pour savoir quel utilisateur
 * effectue les opérations de création et modification.
 */
class ApplicationAuditorAware implements AuditorAware<String> {

    /**
     * Retourne l'identifiant (email) de l'utilisateur actuellement connecté.
     * Si aucun utilisateur n'est connecté, retourne "SYSTEM"
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null 
            || !authentication.isAuthenticated() 
            || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("SYSTEM");
        }
        
        // Retourne le nom de l'utilisateur (généralement son email)
        String auditor = authentication.getName();
        return Optional.of(auditor != null ? auditor : "SYSTEM");
    }
}
