package com.raoudate.GestionDeTri.services;
import com.raoudate.GestionDeTri.auth.ChangePasswordRequest;
import com.raoudate.GestionDeTri.model.User;
import com.raoudate.GestionDeTri.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final EmailService emailService;

    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // check if the current password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        // check if the two new passwords are the same
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        // update the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // save the new password
        repository.save(user);
    }

    // Récupérer tous les utilisateurs
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");
        return repository.findAll();
    }

    // Récupérer un utilisateur par ID
    @Transactional(readOnly = true)
    public User getUserById(Integer id) {
        log.info("Récupération de l'utilisateur avec l'ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + id));
    }

    // Activer un compte utilisateur
    @Transactional
    public User enableUser(Integer id) {
        log.info("Activation du compte utilisateur ID: {}", id);
        User user = getUserById(id);
        user.setEnabled(true);
        User savedUser = repository.save(user);
        
        // Envoyer un email de notification
        try {
            emailService.sendAccountStatusEmail(
                user.getEmail(),
                user.nomComplet(),
                "activé"
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de notification: {}", e.getMessage());
        }
        
        return savedUser;
    }

    // Désactiver un compte utilisateur
    @Transactional
    public User disableUser(Integer id) {
        log.info("Désactivation du compte utilisateur ID: {}", id);
        User user = getUserById(id);
        user.setEnabled(false);
        User savedUser = repository.save(user);
        
        // Envoyer un email de notification
        try {
            emailService.sendAccountStatusEmail(
                user.getEmail(),
                user.nomComplet(),
                "désactivé"
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de notification: {}", e.getMessage());
        }
        
        return savedUser;
    }

    // Bloquer un compte utilisateur
    @Transactional
    public User lockUser(Integer id, String reason) {
        log.info("Blocage du compte utilisateur ID: {} - Raison: {}", id, reason);
        User user = getUserById(id);
        user.setAccountLocked(true);
        User savedUser = repository.save(user);
        
        // Envoyer un email de notification avec la raison
        try {
            emailService.sendAccountLockedEmail(
                user.getEmail(),
                user.nomComplet(),
                reason != null ? reason : "Non spécifiée"
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de notification: {}", e.getMessage());
        }
        
        return savedUser;
    }

    // Débloquer un compte utilisateur
    @Transactional
    public User unlockUser(Integer id) {
        log.info("Déblocage du compte utilisateur ID: {}", id);
        User user = getUserById(id);
        user.setAccountLocked(false);
        User savedUser = repository.save(user);
        
        // Envoyer un email de notification
        try {
            emailService.sendAccountStatusEmail(
                user.getEmail(),
                user.nomComplet(),
                "débloqué"
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de notification: {}", e.getMessage());
        }
        
        return savedUser;
    }

    // Réinitialiser le mot de passe
    @Transactional
    public void resetPassword(Integer id) {
        log.info("Réinitialisation du mot de passe pour l'utilisateur ID: {}", id);
        User user = getUserById(id);
        
        // Générer un token de réinitialisation
        String resetToken = java.util.UUID.randomUUID().toString();
        
        // TODO: Stocker le token dans une table dédiée avec une expiration
        
        // Envoyer l'email de réinitialisation
        try {
            emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.nomComplet(),
                resetToken
            );
            log.info("Email de réinitialisation envoyé à: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation: {}", e.getMessage());
            throw new IllegalStateException("Impossible d'envoyer l'email de réinitialisation");
        }
    }

    // Supprimer un utilisateur
    @Transactional
    public void deleteUser(Integer id) {
        log.info("Suppression de l'utilisateur ID: {}", id);
        User user = getUserById(id);
        
        // Vérifications de sécurité (optionnel)
        // Par exemple, empêcher la suppression du dernier admin
        
        repository.delete(user);
        log.info("Utilisateur supprimé avec succès: {}", user.getEmail());
    }

    // Modifier un utilisateur
    @Transactional
    public User updateUser(Integer id, User updatedUser) {
        log.info("Mise à jour de l'utilisateur ID: {}", id);
        User existingUser = getUserById(id);
        
        // Mettre à jour uniquement les champs modifiables
        if (updatedUser.getNom() != null) {
            existingUser.setNom(updatedUser.getNom());
        }
        if (updatedUser.getPrenom() != null) {
            existingUser.setPrenom(updatedUser.getPrenom());
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(existingUser.getEmail())) {
            // Vérifier que le nouvel email n'existe pas déjà
            if (repository.findByEmail(updatedUser.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Cet email est déjà utilisé");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getNumTel() != null) {
            existingUser.setNumTel(updatedUser.getNumTel());
        }
        if (updatedUser.getDateNaissance() != null) {
            existingUser.setDateNaissance(updatedUser.getDateNaissance());
        }
        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }
        
        return repository.save(existingUser);
    }
}