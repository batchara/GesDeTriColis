package com.raoudate.GestionDeTri.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "agences")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_deleted = false")
public class Agences extends AbstractEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "num_tel", length = 20)
    private String tel;

    @Column(name = "adresse_complete")
    private String adresseComplete;

    @Column(name = "code_bureau")
    private String codeBureau;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "region")
    private String region;

    @Column(name = "responsable")
    private String responsable;

    @Column(name = "email")
    private String email;

    @Column(name = "status")
    private String status;

    @Column(name = "is_active")
    private Boolean isActive;

    // Getter et Setter pour les anciens noms de colonnes (pour compatibilité)
    public String getNom() {
        return label;
    }

    public void setNom(String nom) {
        this.label = nom;
    }

    public String getAdresse() {
        return adresseComplete;
    }

    public void setAdresse(String adresse) {
        this.adresseComplete = adresse;
    }

    public String getTelephone() {
        return tel;
    }

    public void setTelephone(String telephone) {
        this.tel = telephone;
    }
}
