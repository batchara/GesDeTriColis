package com.raoudate.GestionDeTri.model;

import com.raoudate.GestionDeTri.Enum.TypeAdresse;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "adresses",
        indexes = {
                @Index(name = "idx_adresse_ville", columnList = "ville"),
                @Index(name = "idx_adresse_lat_lng", columnList = "latitude, longitude")
        })

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adresse extends AbstractEntity{


    @Column(length = 300, nullable = false)
    private String adresseComplete;   
    @Column(length = 100)
    private String quartier;

    @Column(length = 100)
    private String ville;

    @Column(length = 20)
    private String codePostale;      

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TypeAdresse typeAdresse;  

    private Double latitude;
    private Double longitude;

    @OneToOne(mappedBy = "adresse")
    private Agences agence;
}
