package com.raoudate.GestionDeTri.model;

import com.raoudate.GestionDeTri.Enum.Permission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
// no many-to-many with Role
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.ManyToMany;
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
    private Permission nom;

    @Column(name = "description")
    @ManyToMany(mappedBy = "permissions")
    private List<Role> roles = new ArrayList<>();
    private String description;

}
