package com.raoudate.GestionDeTri.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.raoudate.GestionDeTri.services.impl.RoleService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    /**
     * Endpoint pour initialiser les permissions des rôles ADMIN, SUPERVISEUR et OPERATEUR
     * Accessible uniquement aux administrateurs
     */
    @PostMapping("/initialize-permissions")
    //@PreAuthorize("hasAuthority('ADMIN_CREATE')")
    public ResponseEntity<Map<String, String>> initializePermissions() {
        roleService.initializeRolePermissions();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Les permissions des rôles ont été initialisées avec succès");
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }
}
