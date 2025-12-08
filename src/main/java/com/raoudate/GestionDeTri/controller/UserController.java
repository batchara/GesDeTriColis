package com.raoudate.GestionDeTri.controller;
import com.raoudate.GestionDeTri.auth.ChangePasswordRequest;
import com.raoudate.GestionDeTri.dto.request.CreateUserRequest;
import com.raoudate.GestionDeTri.model.User;
import com.raoudate.GestionDeTri.services.UserServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImp service;

    /**
     * Récupérer tous les utilisateurs (accessible aux ADMIN et SUPERVISEUR)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = service.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Rechercher des utilisateurs par nom, prénom ou email
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        List<User> users = service.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    /**
     * Récupérer un utilisateur par son ID (accessible aux ADMIN et SUPERVISEUR)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        User user = service.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Créer un nouvel utilisateur (accessible aux ADMIN et SUPERVISEUR)
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User createdUser = service.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Modifier un utilisateur existant (accessible uniquement aux ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody CreateUserRequest request) {
        User updatedUser = service.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Mettre à jour les rôles d'un utilisateur (accessible uniquement aux ADMIN)
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<User> updateUserRoles(@PathVariable Integer id, @RequestBody List<String> roles) {
        User updatedUser = service.updateUserRoles(id, roles);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Supprimer un utilisateur (accessible uniquement aux ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restaurer un utilisateur supprimé (soft delete) (accessible uniquement aux ADMIN)
     */
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<User> restoreUser(@PathVariable Integer id) {
        User restoredUser = service.restoreUser(id);
        return ResponseEntity.ok(restoredUser);
    }

    /**
     * Verrouiller un compte utilisateur (accessible aux ADMIN et SUPERVISEUR)
     */
    @PutMapping("/{id}/lock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<Void> lockUser(@PathVariable Integer id) {
        service.lockUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Déverrouiller un compte utilisateur (accessible aux ADMIN et SUPERVISEUR)
     */
    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<Void> unlockUser(@PathVariable Integer id) {
        service.unlockUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Activer un utilisateur (accessible aux ADMIN et SUPERVISEUR)
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<Void> enableUser(@PathVariable Integer id) {
        service.enableUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Désactiver un utilisateur (accessible aux ADMIN et SUPERVISEUR)
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<Void> disableUser(@PathVariable Integer id) {
        service.disableUser(id);
        return ResponseEntity.ok().build();
    }

    
    @PatchMapping
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal connectedUser
    ) {
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    /**
     * Vérifier si un utilisateur existe par email
     */
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> checkUserExistsByEmail(@PathVariable String email) {
        boolean exists = service.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    /**
     * Récupérer le profil de l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUserProfile(Principal connectedUser) {
        if (connectedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Le Principal contient l'email de l'utilisateur
        String email = connectedUser.getName();
        User user = service.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Mettre à jour le profil de l'utilisateur connecté (informations personnelles uniquement)
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUserProfile(
            @RequestBody CreateUserRequest request,
            Principal connectedUser) {
        if (connectedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Le Principal contient l'email de l'utilisateur
        String email = connectedUser.getName();
        User user = service.getUserByEmail(email);
        
        // L'utilisateur ne peut modifier que ses informations personnelles
        User updatedUser = service.updateUserPersonalInfo(user.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }
}

