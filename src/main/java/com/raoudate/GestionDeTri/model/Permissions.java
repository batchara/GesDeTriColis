package com.raoudate.GestionDeTri.model;

import com.raoudate.GestionDeTri.Enum.permissionName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)

public class Permissions extends AbstractEntity{

    @Enumerated(EnumType.STRING)
    @Column(name = "nom")
    private permissionName nom;

    @Column(name = "description")
    private String description;
}
