package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.model.AbstractEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service pour gérer les opérations d'audit et de soft delete.
 * Ce service permet de marquer les entités comme supprimées sans les supprimer physiquement de la base de données.
 */
@Service
public class AuditService {

    /**
     * Marque une entité comme supprimée (soft delete).
     * Cette méthode :
     * - Définit le flag isDeleted à true
     * - Enregistre la date de suppression
     * - Enregistre qui a effectué la suppression
     * 
     * @param entity L'entité à marquer comme supprimée
     * @param <T> Type de l'entité (doit hériter de AbstractEntity)
     * @return L'entité marquée comme supprimée
     */
    public <T extends AbstractEntity> T softDelete(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne peut pas être null");
        }
        
        entity.setDeleted(true);
        entity.setDeletedAt(Instant.now());
        entity.setDeletedBy(getCurrentUser());
        
        return entity;
    }

    /**
     * Restaure une entité précédemment marquée comme supprimée.
     * Cette méthode réinitialise les champs de suppression.
     * 
     * @param entity L'entité à restaurer
     * @param <T> Type de l'entité (doit hériter de AbstractEntity)
     * @return L'entité restaurée
     */
    public <T extends AbstractEntity> T restore(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne peut pas être null");
        }
        
        entity.setDeleted(false);
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        
        return entity;
    }

    /**
     * Vérifie si une entité est marquée comme supprimée.
     * 
     * @param entity L'entité à vérifier
     * @return true si l'entité est supprimée, false sinon
     */
    public boolean isDeleted(AbstractEntity entity) {
        return entity != null && entity.getDeleted();
    }

    /**
     * Récupère l'utilisateur actuellement connecté.
     * 
     * @return L'email de l'utilisateur connecté ou "SYSTEM" si aucun utilisateur n'est connecté
     */
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
