package com.raoudate.GestionDeTri.services.impl;
import com.raoudate.GestionDeTri.dto.request.CreateUserRequest;
import com.raoudate.GestionDeTri.dto.response.UserDTO;
import com.raoudate.GestionDeTri.exception.BusinessErrorCode;
import com.raoudate.GestionDeTri.exception.BusinessException;
import com.raoudate.GestionDeTri.auth.ChangePasswordRequest;
import com.raoudate.GestionDeTri.email.EmailTemplateName;
import com.raoudate.GestionDeTri.email.EmailsService;
import com.raoudate.GestionDeTri.model.Role;
import com.raoudate.GestionDeTri.model.Token;
import com.raoudate.GestionDeTri.model.User;
import com.raoudate.GestionDeTri.repository.RoleRepository;
import com.raoudate.GestionDeTri.repository.TokenRepository;
import com.raoudate.GestionDeTri.repository.UserRepository;
import com.raoudate.GestionDeTri.services.UserService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final EmailsService emailsService;
    
    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // Vérifier si le mot de passe actuel est correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        // Vérifier si les deux nouveaux mots de passe sont identiques
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // Sauvegarder new password
        repository.save(user);
    }

    /**
     * Récupérer tous les utilisateurs (non supprimés)
     */
    public List<User> getAllUsers() {
        return repository.findAllActive();
    }

    /**
     * Rechercher des utilisateurs par nom, prénom ou email
     * Tri par pertinence : correspondances exactes en premier
     */
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }
        
        String trimmedSearch = searchTerm.trim();
        
        // Si la recherche est trop courte (1-2 caractères), limiter les résultats
        List<User> results = repository.searchUsers(trimmedSearch);
        
        if (trimmedSearch.length() <= 2) {
            // Limiter à 10 résultats pour les recherches courtes
            return results.stream().limit(10).toList();
        }
        
        return results;
    }

    /**
     * Récupérer un utilisateur par son ID
     */
    public User getUserById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être null");
        }
        final Integer finalId = id;
        return repository.findById(finalId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));
    }

    /**
     * Créer un nouvel utilisateur avec envoi d'email d'activation
     */
    @Transactional
    public User createUser(CreateUserRequest request) {
        System.out.println(" [createUser] Début création utilisateur: " + request.getEmail());
        
        // Vérifier si l'email existe déjà (actif)
        if (repository.findActiveByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(BusinessErrorCode.USER_ALREADY_EXISTS);
        }
        
        // Vérifier si un utilisateur supprimé existe avec cet email
        Optional<User> deletedUser = repository.findDeletedByEmail(request.getEmail());
        if (deletedUser.isPresent()) {
            System.out.println(" [createUser] Un utilisateur supprimé existe avec cet email: " + request.getEmail());
            throw new BusinessException(
                BusinessErrorCode.DELETED_USER_EXISTS, 
                "Un utilisateur supprimé existe avec cet email. Utilisez la fonction de restauration (ID: " + deletedUser.get().getId() + ")"
            );
        }

        // Déterminer le nom du rôle
        String roleNameTemp = request.getRole() != null ? request.getRole().toUpperCase() : "OPERATEUR";
        final String roleName = roleNameTemp.startsWith("ROLE_") ? roleNameTemp : "ROLE_" + roleNameTemp;
        
        System.out.println(" [createUser] Recherche du rôle: " + roleName);

        // Récupérer le rôle depuis la base de données
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Rôle " + roleName + " introuvable"));
        
        System.out.println(" [createUser] Rôle trouvé: " + role.getName() + " (ID: " + role.getId() + ")");

        // Utiliser le mot de passe fourni par l'admin (temporaire)
        // Si aucun mot de passe n'est fourni, en générer un automatiquement
        String temporaryPassword = (request.getPassword() != null && !request.getPassword().isEmpty()) 
                ? request.getPassword() 
                : generateTemporaryPassword();
        System.out.println(" [createUser] Mot de passe temporaire : " + (request.getPassword() != null ? "fourni par l'admin" : "généré automatiquement"));

        // Créer le nouvel utilisateur (compte désactivé par défaut)
        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .numTel(request.getNumTel())
                .dateNaissance(request.getDateNaissance())
                .accountLocked(false)
                .enabled(false) // Compte désactivé jusqu'à l'activation
                .mustChangePassword(true) // Forcer le changement de mot de passe
                .roles(new HashSet<>())
                .build();

        // Ajouter le rôle à l'utilisateur
        user.getRoles().add(role);
        
        System.out.println(" [createUser] Rôles assignés avant sauvegarde: " + user.getRoles().size());

        // Sauvegarder l'utilisateur
        User savedUser = repository.save(user);
        
        System.out.println(" [createUser] Utilisateur créé avec ID: " + savedUser.getId());
        System.out.println(" [createUser] Rôles après sauvegarde: " + savedUser.getRoles().size());
        
        // Envoyer l'email d'activation avec le code
        try {
            sendActivationEmail(savedUser, temporaryPassword);
            System.out.println(" [createUser] Email d'activation envoyé à: " + savedUser.getEmail());
        } catch (MessagingException e) {
            System.err.println(" [createUser] Échec envoi email: " + e.getMessage());
            // Supprimer l'utilisateur si l'email ne peut pas être envoyé
            repository.delete(savedUser);
            throw new BusinessException(BusinessErrorCode.EMAIL_SENDING_FAILED);
        }
        
        return savedUser;
    }
    
    /**
     * Générer un mot de passe temporaire sécurisé
     */
    private String generateTemporaryPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&+=!";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);
        
        // Assurer au moins un de chaque type
        password.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(random.nextInt(26))); // Majuscule
        password.append("abcdefghijklmnopqrstuvwxyz".charAt(random.nextInt(26))); // Minuscule
        password.append("0123456789".charAt(random.nextInt(10))); // Chiffre
        password.append("@#$%^&+=!".charAt(random.nextInt(9))); // Caractère spécial
        
        // Remplir le reste
        for (int i = 4; i < 12; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        // Mélanger les caractères
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * Envoyer l'email d'activation avec le code
     */
    private void sendActivationEmail(User user, String temporaryPassword) throws MessagingException {
        // Générer un code d'activation à 6 chiffres
        String activationCode = generateActivationCode();
        
        // Sauvegarder le token d'activation avec le mot de passe temporaire
        Token token = Token.builder()
                .token(activationCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .temporaryPassword(temporaryPassword) // Stocker le mot de passe temporaire
                .user(user)
                .build();
        final Token finalToken = token;
        @SuppressWarnings({"null", "unused"})
        var ignored = tokenRepository.save(finalToken);
        
        // Envoyer l'email avec le code d'activation uniquement
        emailsService.sendEmail(
                user.getEmail(),
                user.nomComplet(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                activationCode,
                "Activation de votre compte - Société des Postes du Togo"
        );
        
        System.out.println(" Email d'activation envoyé avec le code: " + activationCode);
        System.out.println(" Mot de passe temporaire stocké dans le token");
    }
    
    /**
     * Générer un code d'activation à 6 chiffres
     */
    private String generateActivationCode() {
        String characters = "0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(6);
        
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return code.toString();
    }

    /**
     * Mettre à jour un utilisateur existant
     */
    public User updateUser(Integer id, CreateUserRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être null");
        }
        final Integer finalId = id;
        User user = repository.findById(finalId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));

        // Mettre à jour les champs
        if (request.getNom() != null) user.setNom(request.getNom());
        if (request.getPrenom() != null) user.setPrenom(request.getPrenom());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getNumTel() != null) user.setNumTel(request.getNumTel());
        if (request.getDateNaissance() != null) user.setDateNaissance(request.getDateNaissance());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());
        if (request.getAccountLocked() != null) user.setAccountLocked(request.getAccountLocked());

        // Mettre à jour le mot de passe si fourni
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Mettre à jour le rôle si fourni
        if (request.getRole() != null) {
            String roleNameTemp = request.getRole().toUpperCase();
            final String roleName = roleNameTemp.startsWith("ROLE_") ? roleNameTemp : "ROLE_" + roleNameTemp;

            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException("Rôle " + roleName + " introuvable"));

            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
        }

        final User finalUser = user;
        @SuppressWarnings("null")
        User updatedUser = repository.save(finalUser);
        System.out.println(" Utilisateur mis à jour avec succès: " + updatedUser.getEmail());
        return updatedUser;
    }

    /**
     * Mettre à jour uniquement les rôles d'un utilisateur
     */
    public User updateUserRoles(Integer id, List<String> roleNames) {
        System.out.println(" [updateUserRoles] Début - User ID: " + id + ", Rôles demandés: " + roleNames);
        
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être null");
        }
        final Integer finalId = id;
        User user = repository.findById(finalId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));

        // Construire le set de rôles
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : roleNames) {
            String tempRoleName = roleName.toUpperCase();
            final String normalizedRoleName = tempRoleName.startsWith("ROLE_") ? tempRoleName : "ROLE_" + tempRoleName;
            
            System.out.println(" [updateUserRoles] Recherche du rôle: " + normalizedRoleName);
            Role role = roleRepository.findByName(normalizedRoleName)
                    .orElseThrow(() -> new IllegalStateException("Rôle " + normalizedRoleName + " introuvable"));
            
            newRoles.add(role);
            System.out.println(" [updateUserRoles] Rôle trouvé: " + role.getName());
        }

        // Remplacer les rôles existants
        user.setRoles(newRoles);
        User savedUser = repository.save(user);
        
        System.out.println(" [updateUserRoles] Rôles mis à jour avec succès pour: " + savedUser.getEmail());
        System.out.println(" [updateUserRoles] Nouveaux rôles: " + savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.joining(", ")));
        
        return savedUser;
    }

    /**
     * Supprimer un utilisateur par son ID
     */
    public void deleteUser(Integer id) {
        System.out.println(" [deleteUser] Début - User ID: " + id);
        
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être null");
        }
        final Integer finalId = id;
        // Vérifier que l'utilisateur existe
        User user = repository.findById(finalId)
                .orElseThrow(() -> {
                    System.out.println(" [deleteUser] Utilisateur introuvable - ID: " + id);
                    return new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable");
                });
        
        System.out.println(" [deleteUser] Utilisateur trouvé: " + user.getEmail());
        System.out.println(" [deleteUser] Nom: " + user.getPrenom() + " " + user.getNom());
        
        // Soft delete : marquer l'utilisateur comme supprimé au lieu de le supprimer physiquement
        user.setDeleted(true);
        user.setDeletedAt(java.time.Instant.now());
        
        // Récupérer l'utilisateur connecté pour traçabilité
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            user.setDeletedBy(authentication.getName());
        } else {
            user.setDeletedBy("SYSTEM");
        }
        
        repository.save(user);
        
        System.out.println(" [deleteUser] Utilisateur marqué comme supprimé (soft delete): " + user.getEmail());
        System.out.println(" [deleteUser] Supprimé par: " + user.getDeletedBy() + " à " + user.getDeletedAt());
    }

    /**
     * Restaurer un utilisateur supprimé (soft delete)
     */
    @Transactional
    public User restoreUser(Integer id) {
        System.out.println(" [restoreUser] Début restauration - User ID: " + id);
        
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être null");
        }
        final Integer finalId = id;
        // Vérifier que l'utilisateur existe
        User user = repository.findById(finalId)
                .orElseThrow(() -> {
                    System.out.println(" [restoreUser] Utilisateur introuvable - ID: " + id);
                    return new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable");
                });
        
        // Vérifier qu'il est bien supprimé
        if (!user.getDeleted()) {
            System.out.println(" [restoreUser] Utilisateur déjà actif - ID: " + id);
            throw new IllegalStateException("L'utilisateur n'est pas supprimé");
        }
        
        System.out.println(" [restoreUser] Utilisateur trouvé: " + user.getEmail());
        System.out.println(" [restoreUser] Supprimé le: " + user.getDeletedAt() + " par: " + user.getDeletedBy());
        
        // Restaurer l'utilisateur
        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setDeletedBy(null);
        user.setEnabled(true); // Réactiver le compte
        
        // Récupérer l'utilisateur connecté pour traçabilité
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String restoredBy = (authentication != null && authentication.isAuthenticated()) 
                ? authentication.getName() 
                : "SYSTEM";
        
        User savedUser = repository.save(user);
        
        System.out.println(" [restoreUser] Utilisateur restauré: " + savedUser.getEmail());
        System.out.println(" [restoreUser] Restauré par: " + restoredBy);
        
        return savedUser;
    }

    /**
     * Activer un utilisateur
     */
    public void enableUser(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être null");
        }
        final Integer finalId = id;
        User user = repository.findById(finalId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));
        
        user.setEnabled(true);
        repository.save(user);
        System.out.println(" [enableUser] Utilisateur activé: " + user.getEmail());
    }

    /**
     * Désactiver un utilisateur
     */
    public void disableUser(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être null");
        }
        final Integer finalId = id;
        User user = repository.findById(finalId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));
        
        user.setEnabled(false);
        repository.save(user);
        System.out.println(" [disableUser] Utilisateur désactivé: " + user.getEmail());
    }

    /**
     * Verrouiller un utilisateur
     */
    public void lockUser(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être null");
        }
        final Integer finalId = id;
        User user = repository.findById(finalId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));
        
        user.setAccountLocked(true);
        repository.save(user);
        System.out.println(" [lockUser] Utilisateur verrouillé: " + user.getEmail());
    }

    /**
     *  Déverrouiller un utilisateur et réinitialiser le compteur de tentatives
     */
    public void unlockUser(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être null");
        }
        final Integer finalId = id;
        User user = repository.findById(finalId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));
        
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockTime(null);
        user.setLastFailedLogin(null);
        repository.save(user);
        System.out.println(" [unlockUser] Utilisateur déverrouillé et compteur réinitialisé: " + user.getEmail());
    }

    @Override
    public UserDTO save(UserDTO userDTO) {
        return null;
    }

    @Override
    public UserDTO findById(Integer id) {
        return null;
    }

    @Override
    public UserDTO findByUsername(String username) {
        return null;
    }

    @Override
    public List<UserDTO> findAll() {
        return List.of();
    }

    @Override
    public List<UserDTO> searchByEmail(String email) {
        return List.of();
    }

    @Override
    public UserDTO update(Integer id, UserDTO userDTO) {
        return null;
    }

    @Override
    public List<UserDTO> search(String term) {
        return List.of();
    }

    @Override
    public void delete(Integer id) {

    }

    /**
     * Vérifier si un utilisateur existe par email
     */
    public boolean existsByEmail(String email) {
        return repository.findByEmail(email).isPresent();
    }

    /**
     * Récupérer un utilisateur par son email
     */
    public User getUserByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'email " + email + " introuvable"));
    }

    /**
     * Mettre à jour uniquement les informations personnelles d'un utilisateur
     * (nom, prénom, date de naissance, numéro de téléphone)
     * Cette méthode est utilisée lorsqu'un utilisateur modifie son propre profil
     */
    public User updateUserPersonalInfo(Integer id, CreateUserRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être null");
        }
        final Integer finalId = id;
        User user = repository.findById(finalId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));

        // Mettre à jour uniquement les informations personnelles
        if (request.getNom() != null) user.setNom(request.getNom());
        if (request.getPrenom() != null) user.setPrenom(request.getPrenom());
        if (request.getDateNaissance() != null) user.setDateNaissance(request.getDateNaissance());
        if (request.getNumTel() != null) user.setNumTel(request.getNumTel());

        // NE PAS permettre la modification de :
        // - l'email
        // - le mot de passe (utiliser le endpoint dédié)
        // - les rôles
        // - le statut enabled/locked

        final User finalUser = user;
        @SuppressWarnings("null")
        User updatedUser = repository.save(finalUser);
        System.out.println(" Informations personnelles mises à jour pour: " + updatedUser.getEmail());
        return updatedUser;
    }
}

