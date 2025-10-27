package com.raoudate.GestionDeTri.model;

import com.raoudate.GestionDeTri.Enum.StatutColis;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "colis")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Colis extends AbstractEntity {

    @Column(name = "code_suivi", unique = true, nullable = false)
    private String codeSuivi;

    @Column(name = "poids")
    private BigDecimal poids;

    @Column(name = "nom_exp")
    private String nomExp;

    @Column(name = "nom_dest", nullable = false)
    private String nomDest;

    @Column(name = "tel_dest")
    private String telDest;

    @Column(name = "date_envoi")
    private Instant dateEnvoi;

    @Column(name = "date_prevue")
    private Instant datePrevue;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutColis statut;

    @Column(name = "adresse_dest")
    private String adresseDest;

    @ManyToOne
    @JoinColumn(name = "operateur_id")
    private Operateur operateur;

    @ManyToOne
    @JoinColumn(name = "centre_tri_id")
    private CentreDeTri centreTri;

    @ManyToOne
    @JoinColumn(name = "agence_affectee_id")
    private Agences agenceAffectee;
}
