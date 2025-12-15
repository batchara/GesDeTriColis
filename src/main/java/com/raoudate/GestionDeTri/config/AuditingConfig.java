package com.raoudate.GestionDeTri.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;


@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new ApplicationAuditorAware();
    }
}


class ApplicationAuditorAware implements AuditorAware<String> {

    
    @Override
    @NonNull
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
