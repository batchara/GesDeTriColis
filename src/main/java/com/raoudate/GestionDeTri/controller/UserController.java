package com.raoudate.GestionDeTri.controller;
import com.raoudate.GestionDeTri.auth.ChangePasswordRequest;
import com.raoudate.GestionDeTri.Dto.CreateUserRequest;
import com.raoudate.GestionDeTri.model.User;
import com.raoudate.GestionDeTri.services.UserServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImp service;



    @GetMapping("/test-controller")
    public void testController() {
        System.out.println("********** TESTING USER CONTROLLER CONSTRUCTOR **********");

    }

    /**
     * Récupérer tous les utilisateurs (accessible uniquement aux ADMIN)
     */
    @GetMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {

        System.out.println("********** TESTING USER CONTROLLER **********");
        List<User> users = service.getAllUsers();
        System.out.println("********** TESTING USER CONTROLLER **********");
        return ResponseEntity.ok(users);
    }

    /**
     * Récupérer un utilisateur par son ID (accessible aux ADMIN et SUPERVISEUR)
     */
    @GetMapping("/{id}")
   // @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        User user = service.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Créer un nouvel utilisateur (accessible uniquement aux ADMIN)
     */
    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        System.out.println("📝 [UserController] Création d'un nouvel utilisateur: " + request.getEmail());
        User createdUser = service.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Modifier un utilisateur existant (accessible uniquement aux ADMIN)
     */
    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody CreateUserRequest request) {
        System.out.println("🔄 [UserController] Mise à jour utilisateur ID: " + id);
        User updatedUser = service.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Mettre à jour les rôles d'un utilisateur (accessible uniquement aux ADMIN)
     */
    @PutMapping("/{id}/roles")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRoles(@PathVariable Integer id, @RequestBody List<String> roles) {
        System.out.println("🔄 [UserController] Mise à jour des rôles pour utilisateur ID: " + id + " - Rôles: " + roles);
        User updatedUser = service.updateUserRoles(id, roles);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Supprimer un utilisateur (accessible uniquement aux ADMIN)
     */
    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        System.out.println("🗑️ [UserController] Suppression de l'utilisateur ID: " + id);
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal connectedUser
    ) {
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }
}