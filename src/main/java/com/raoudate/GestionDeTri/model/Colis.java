package com.raoudate.GestionDeTri.model;

import com.raoudate.GestionDeTri.Enum.StatutColis;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(callSuper = true)


@Table(name = "colis")

public class Colis extends AbstractEntity {
    @Column(nullable = false, unique = true, length = 60)
    private String codeSuivi;           // CodeSuiv

    @Column
    private BigDecimal poids;               // en kg (si utile)

    // Infos expéditeur / destinataire (du cahier de charge)
    @Column(length = 120)
    private String nomExp;

    @Column(length = 120)
    private String nomDest;

    @Column(length = 40)
    private String telDest;

    private Instant dateEnvoi;

    private Instant datePrevue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutColis statut = StatutColis.EN_ATTENTE;

    // OCR brut avant nettoyage/géocodage
    @Column(length = 300)
    private String adresseDetectee;

    // Adresse normalisée (géocodée) = DESTINATAIRE
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "adresse_id")
    private Adresse adresse;


    // Qui a scanné / créé l’enregistrement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operateur_id")
    private Operateur operateur;

    // Centre de tri qui traite ce colis (1 centre -> N colis)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centre_tri_id")
    private CentreDeTri centreTri;

    // Agence affectée automatiquement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id")
    private Agences agenceAffectee;
}

