package com.raoudate.GestionDeTri.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDTO {
    
    private Integer entityId;
    
    private String entityType;
    
    private AuditOperation operation;
    
    private Instant createdDate;
    
    private String createdBy;
    
    private Instant lastModifiedDate;
    
    private String lastModifiedBy;
    
    private Instant deletedAt;
 
    private String deletedBy;
    
    private boolean deleted;
    
    private String entityDescription;
    
    private String comment;
   
    public enum AuditOperation {
        CREATE,      // Création de l'entité
        UPDATE,      // Modification de l'entité
        DELETE,      // Suppression (soft delete) de l'entité
        RESTORE,     // Restauration d'une entité supprimée
        READ         // Consultation (si tracking de lecture activé)
    }
    
   
    public Long getLifetimeInSeconds() {
        if (createdDate == null) {
            return null;
        }
        Instant endDate = deletedAt != null ? deletedAt : Instant.now();
        return endDate.getEpochSecond() - createdDate.getEpochSecond();
    }
    
  
    public boolean hasBeenModified() {
        return lastModifiedDate != null && 
               createdDate != null && 
               lastModifiedDate.isAfter(createdDate);
    }
    
    public String getLastActor() {
        if (deletedBy != null) return deletedBy;
        if (lastModifiedBy != null) return lastModifiedBy;
        return createdBy;
    }
   
    public Instant getLastActionDate() {
        if (deletedAt != null) return deletedAt;
        if (lastModifiedDate != null) return lastModifiedDate;
        return createdDate;
    }
}
