package com.raoudate.GestionDeTri.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)

@Table(name = "centreDeTri")


public class CentreDeTri extends AbstractEntity {
    @Column(name = "nom")
    private String nom;

    @Column(name = "telephone", length = 15)
    private String telephone;

    @Column(name ="adresseCentre ")
    private String adresseCentre;

    private BigDecimal latitude;

    private BigDecimal longitude;
}
