package com.raoudate.GestionDeTri.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)


@Table(name = "agences")

public class Agences extends AbstractEntity {
    

    @Column(name = "code")
    private String code;

    @Column(name = "email")
    private String email;

    @Column(name = "num_tel")
    private String tel;

     @Column(length = 100)
    private String region;

     @Column(length = 100)
    private String label;

    private Double latitude;
    private Double longitude;
    
    @Column(length = 300, nullable = false)
    private String adresseComplete;   



}
