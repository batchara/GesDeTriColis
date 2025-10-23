package com.raoudate.GestionDeTri.controller;
import com.raoudate.GestionDeTri.auth.ChangePasswordRequest;
import com.raoudate.GestionDeTri.model.User;
import com.raoudate.GestionDeTri.services.UserServiceImp;
import lombok.RequiredArgsConstructor;
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

    @PatchMapping
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal connectedUser
    ) {
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }
}