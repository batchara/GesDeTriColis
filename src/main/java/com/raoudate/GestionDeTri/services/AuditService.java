package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.model.AbstractEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class AuditService {

    
    
    public <T extends AbstractEntity> T softDelete(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne peut pas être null");
        }
        
        entity.setDeleted(true);
        entity.setDeletedAt(Instant.now());
        entity.setDeletedBy(getCurrentUser());
        
        return entity;
    }
    public <T extends AbstractEntity> T restore(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne peut pas être null");
        }
        
        entity.setDeleted(false);
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        
        return entity;
    }
    public boolean isDeleted(AbstractEntity entity) {
        return entity != null && entity.getDeleted();
    }

   
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null 
            || !authentication.isAuthenticated() 
            || authentication instanceof AnonymousAuthenticationToken) {
            return "SYSTEM";
        }
        
        String username = authentication.getName();
        return username != null ? username : "SYSTEM";
    }
}
