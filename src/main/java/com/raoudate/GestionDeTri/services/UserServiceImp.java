package com.raoudate.GestionDeTri.services;
import com.raoudate.GestionDeTri.Dto.CreateUserRequest;
import com.raoudate.GestionDeTri.Dto.UserDTO;
import com.raoudate.GestionDeTri.auth.ChangePasswordRequest;
import com.raoudate.GestionDeTri.model.Role;
import com.raoudate.GestionDeTri.model.User;
import com.raoudate.GestionDeTri.repository.RoleRepository;
import com.raoudate.GestionDeTri.repository.UserRepository;
import com.raoudate.GestionDeTri.services.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final RoleRepository roleRepository;
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

    /**
     * Récupérer tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    /**
     * Récupérer un utilisateur par son ID
     */
    public User getUserById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));
    }

    /**
     * Créer un nouvel utilisateur
     */
    public User createUser(CreateUserRequest request) {
        System.out.println("🔵 [createUser] Début création utilisateur: " + request.getEmail());
        
        // Vérifier si l'email existe déjà
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Un utilisateur avec cet email existe déjà");
        }

        // Déterminer le nom du rôle
        String roleNameTemp = request.getRole() != null ? request.getRole().toUpperCase() : "OPERATEUR";
        final String roleName = roleNameTemp.startsWith("ROLE_") ? roleNameTemp : "ROLE_" + roleNameTemp;
        
        System.out.println("🔵 [createUser] Recherche du rôle: " + roleName);

        // Récupérer le rôle depuis la base de données
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Rôle " + roleName + " introuvable"));
        
        System.out.println("✅ [createUser] Rôle trouvé: " + role.getName() + " (ID: " + role.getId() + ")");

        // Créer le nouvel utilisateur
        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .numTel(request.getNumTel())
                .dateNaissance(request.getDateNaissance())
                .accountLocked(request.getAccountLocked() != null ? request.getAccountLocked() : false)
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .roles(new HashSet<>()) // Initialiser explicitement
                .build();

        // Ajouter le rôle à l'utilisateur
        user.getRoles().add(role);
        
        System.out.println("🔵 [createUser] Rôles assignés avant sauvegarde: " + user.getRoles().size());

        // Sauvegarder l'utilisateur
        User savedUser = repository.save(user);
        
        System.out.println("✅ [createUser] Utilisateur créé avec ID: " + savedUser.getId());
        System.out.println("✅ [createUser] Rôles après sauvegarde: " + savedUser.getRoles().size());
        
        return savedUser;
    }

    /**
     * Mettre à jour un utilisateur existant
     */
    public User updateUser(Integer id, CreateUserRequest request) {
        User user = repository.findById(id)
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

        User updatedUser = repository.save(user);
        System.out.println("✅ Utilisateur mis à jour avec succès: " + updatedUser.getEmail());
        return updatedUser;
    }

    /**
     * Mettre à jour uniquement les rôles d'un utilisateur
     */
    public User updateUserRoles(Integer id, List<String> roleNames) {
        System.out.println("🔄 [updateUserRoles] Début - User ID: " + id + ", Rôles demandés: " + roleNames);
        
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));

        // Construire le set de rôles
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : roleNames) {
            String tempRoleName = roleName.toUpperCase();
            final String normalizedRoleName = tempRoleName.startsWith("ROLE_") ? tempRoleName : "ROLE_" + tempRoleName;
            
            System.out.println("🔍 [updateUserRoles] Recherche du rôle: " + normalizedRoleName);
            Role role = roleRepository.findByName(normalizedRoleName)
                    .orElseThrow(() -> new IllegalStateException("Rôle " + normalizedRoleName + " introuvable"));
            
            newRoles.add(role);
            System.out.println("✅ [updateUserRoles] Rôle trouvé: " + role.getName());
        }

        // Remplacer les rôles existants
        user.setRoles(newRoles);
        User savedUser = repository.save(user);
        
        System.out.println("✅ [updateUserRoles] Rôles mis à jour avec succès pour: " + savedUser.getEmail());
        System.out.println("✅ [updateUserRoles] Nouveaux rôles: " + savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.joining(", ")));
        
        return savedUser;
    }

    /**
     * Supprimer un utilisateur par son ID
     */
    public void deleteUser(Integer id) {
        System.out.println("🗑️ [deleteUser] Début - User ID: " + id);
        
        // Vérifier que l'utilisateur existe
        User user = repository.findById(id)
                .orElseThrow(() -> {
                    System.out.println("❌ [deleteUser] Utilisateur introuvable - ID: " + id);
                    return new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable");
                });
        
        System.out.println("🔍 [deleteUser] Utilisateur trouvé: " + user.getEmail());
        System.out.println("🔍 [deleteUser] Nom: " + user.getPrenom() + " " + user.getNom());
        
        // Supprimer l'utilisateur (cascade delete s'occupera des relations)
        repository.delete(user);
        
        System.out.println("✅ [deleteUser] Utilisateur supprimé avec succès: " + user.getEmail());
    }

    /**
     * Activer un utilisateur
     */
    public void enableUser(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));
        
        user.setEnabled(true);
        repository.save(user);
        System.out.println("✅ [enableUser] Utilisateur activé: " + user.getEmail());
    }

    /**
     * Désactiver un utilisateur
     */
    public void disableUser(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));
        
        user.setEnabled(false);
        repository.save(user);
        System.out.println("❌ [disableUser] Utilisateur désactivé: " + user.getEmail());
    }

    /**
     * Verrouiller un utilisateur
     */
    public void lockUser(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));
        
        user.setAccountLocked(true);
        repository.save(user);
        System.out.println("🔒 [lockUser] Utilisateur verrouillé: " + user.getEmail());
    }

    /**
     * Déverrouiller un utilisateur
     */
    public void unlockUser(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));
        
        user.setAccountLocked(false);
        repository.save(user);
        System.out.println("🔓 [unlockUser] Utilisateur déverrouillé: " + user.getEmail());
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
}