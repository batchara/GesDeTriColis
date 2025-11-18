package com.raoudate.GestionDeTri.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AbstractEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ========== Audit Temporel ==========
    
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant dateCreation;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Instant dateModification;
    
    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ========== Audit Utilisateur ==========
    
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
    
    @Column(name = "deleted_by")
    private String deletedBy;
    
    // ========== Soft Delete ==========
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

}
