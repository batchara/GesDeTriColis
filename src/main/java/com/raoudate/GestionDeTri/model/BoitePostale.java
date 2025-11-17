package com.raoudate.GestionDeTri.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "boites_postales")
@SQLRestriction("deleted_at IS NULL")
public class BoitePostale extends AbstractEntity {
    
    @Column(name = "libelle", length = 200)
    private String libelle;
    
    @Column(name = "capacite")
    private Integer capacite;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id", nullable = false)
    private Agences agence;
}
