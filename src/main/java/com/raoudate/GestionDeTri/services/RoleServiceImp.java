package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.enums.Permission;
import com.raoudate.GestionDeTri.enums.RoleType;
import com.raoudate.GestionDeTri.model.Permissions;
import com.raoudate.GestionDeTri.model.Role;
import com.raoudate.GestionDeTri.repository.PermissionRepository;
import com.raoudate.GestionDeTri.repository.RoleRepository;
import com.raoudate.GestionDeTri.services.impl.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImp implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Initialise les permissions pour tous les rôles (ADMIN, SUPERVISEUR, OPERATEUR)
     * Cette méthode crée les permissions si elles n'existent pas et les associe aux rôles correspondants
     */
    @Override
    @Transactional
    public void initializeRolePermissions() {
        log.info("Début de l'initialisation des permissions pour les rôles...");

        // 1. Créer toutes les permissions si elles n'existent pas
        createPermissionsIfNotExist();

        // 2. Initialiser les permissions pour chaque rôle
        initializePermissionsForRole(RoleType.ADMIN);
        initializePermissionsForRole(RoleType.SUPERVISEUR);
        initializePermissionsForRole(RoleType.OPERATEUR);

        log.info("Initialisation des permissions terminée avec succès");
    }

    /**
     * Crée toutes les permissions définies dans l'enum Permission si elles n'existent pas
     */
    private void createPermissionsIfNotExist() {
        for (Permission permission : Permission.values()) {
            if (!permissionRepository.existsByNom(permission)) {
                Permissions newPermission = new Permissions(permission, permission.getDescription());
                permissionRepository.save(newPermission);
                log.info("Permission créée : {} - {}", permission.name(), permission.getDescription());
            }
        }
    }

    /**
     * Initialise les permissions pour un rôle spécifique
     * @param roleType Le type de rôle (ADMIN, SUPERVISEUR, OPERATEUR)
     */
    private void initializePermissionsForRole(RoleType roleType) {
        String roleName = "ROLE_" + roleType.name();
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException(
                        "Le rôle " + roleName + " n'existe pas. Veuillez d'abord créer le rôle."
                ));

        // Récupérer les permissions définies pour ce type de rôle
        List<Permissions> permissionsToAssign = new ArrayList<>();
        for (Permission permission : roleType.getPermissions()) {
            Permissions permissionEntity = permissionRepository.findByNom(permission)
                    .orElseThrow(() -> new IllegalStateException(
                            "Permission " + permission.name() + " introuvable"
                    ));
            permissionsToAssign.add(permissionEntity);
        }

        // Vider les anciennes permissions et assigner les nouvelles
        role.getPermissions().clear();
        role.getPermissions().addAll(permissionsToAssign);
        roleRepository.save(role);

        log.info("Permissions initialisées pour le rôle {} : {} permission(s) assignée(s)", 
                roleName, permissionsToAssign.size());
    }
}
