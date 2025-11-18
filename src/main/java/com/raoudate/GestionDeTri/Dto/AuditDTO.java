package com.raoudate.GestionDeTri.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO pour représenter les informations d'audit d'une entité.
 * Utilisé pour les rapports d'audit et l'historique des modifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDTO {
    
    /**
     * ID de l'entité auditée
     */
    private Integer entityId;
    
    /**
     * Type de l'entité (User, Agence, Colis, etc.)
     */
    private String entityType;
    
    /**
     * Type d'opération (CREATE, UPDATE, DELETE, RESTORE)
     */
    private AuditOperation operation;
    
    // ========== Informations de création ==========
    
    /**
     * Date et heure de création
     */
    private Instant createdDate;
    
    /**
     * Utilisateur qui a créé l'entité
     */
    private String createdBy;
    
    // ========== Informations de modification ==========
    
    /**
     * Date et heure de dernière modification
     */
    private Instant lastModifiedDate;
    
    /**
     * Utilisateur qui a modifié l'entité en dernier
     */
    private String lastModifiedBy;
    
    // ========== Informations de suppression (soft delete) ==========
    
    /**
     * Date et heure de suppression
     */
    private Instant deletedAt;
    
    /**
     * Utilisateur qui a supprimé l'entité
     */
    private String deletedBy;
    
    /**
     * Indique si l'entité est supprimée
     */
    private boolean deleted;
    
    // ========== Informations additionnelles ==========
    
    /**
     * Description de l'entité (nom, code, etc.)
     */
    private String entityDescription;
    
    /**
     * Commentaire ou raison de l'opération (optionnel)
     */
    private String comment;
    
    /**
     * Énumération des types d'opérations d'audit
     */
    public enum AuditOperation {
        CREATE,      // Création de l'entité
        UPDATE,      // Modification de l'entité
        DELETE,      // Suppression (soft delete) de l'entité
        RESTORE,     // Restauration d'une entité supprimée
        READ         // Consultation (si tracking de lecture activé)
    }
    
    /**
     * Calcule la durée de vie de l'entité (de sa création à sa suppression ou maintenant)
     * @return Durée en secondes
     */
    public Long getLifetimeInSeconds() {
        if (createdDate == null) {
            return null;
        }
        Instant endDate = deletedAt != null ? deletedAt : Instant.now();
        return endDate.getEpochSecond() - createdDate.getEpochSecond();
    }
    
    /**
     * Vérifie si l'entité a été modifiée depuis sa création
     * @return true si l'entité a été modifiée
     */
    public boolean hasBeenModified() {
        return lastModifiedDate != null && 
               createdDate != null && 
               lastModifiedDate.isAfter(createdDate);
    }
    
    /**
     * Récupère le dernier utilisateur qui a interagi avec l'entité
     * @return Email du dernier utilisateur (deletedBy > lastModifiedBy > createdBy)
     */
    public String getLastActor() {
        if (deletedBy != null) return deletedBy;
        if (lastModifiedBy != null) return lastModifiedBy;
        return createdBy;
    }
    
    /**
     * Récupère la dernière date d'interaction avec l'entité
     * @return Date de la dernière interaction (deletedAt > lastModifiedDate > createdDate)
     */
    public Instant getLastActionDate() {
        if (deletedAt != null) return deletedAt;
        if (lastModifiedDate != null) return lastModifiedDate;
        return createdDate;
    }
}
