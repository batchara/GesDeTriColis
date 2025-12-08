package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.enums.Permission;
import com.raoudate.GestionDeTri.model.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permissions, Integer> {
    Optional<Permissions> findByNom(Permission nom);
    boolean existsByNom(Permission nom);
}
