package com.raoudate.GestionDeTri.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Set;
import java.util.HashSet;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
public class Role extends AbstractEntity {



    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<User> users;

    

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    @Builder.Default
    private Set<Permissions> permissions = new HashSet<>();


}
