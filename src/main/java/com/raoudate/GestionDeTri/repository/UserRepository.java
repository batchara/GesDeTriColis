package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByNom(String nom);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN u.roles r " +
           "WHERE r.name = :roleName")
    Optional<User> findFirstByRolesName(@Param("roleName") String roleName);
    
    // ========== Méthodes avec gestion du soft delete ==========
    
    /**
     * Récupère tous les utilisateurs non supprimés
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    List<User> findAllActive();
    
    /**
     * Récupère un utilisateur non supprimé par son ID
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findActiveById(@Param("id") Integer id);
    
    // Compter les utilisateurs actifs
    long countByEnabledTrue();
    
    // Compter les utilisateurs bloqués
    long countByAccountLockedTrue();
    
    /**
     * Récupère un utilisateur non supprimé par son email
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findActiveByEmail(@Param("email") String email);
    
    /**
     * Récupère tous les utilisateurs supprimés (pour l'audit)
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = true")
    List<User> findAllDeleted();
    
    /**
     * Récupère un utilisateur supprimé par son email (pour la restauration)
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.email = :email AND u.isDeleted = true")
    Optional<User> findDeletedByEmail(@Param("email") String email);
    
    /**
     * Recherche des utilisateurs par nom, prénom ou email (non supprimés uniquement)
     * Résultats triés par pertinence : nom exact > prénom exact > email exact > contient
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.isDeleted = false AND (" +
           "LOWER(u.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY " +
           "CASE " +
           "  WHEN LOWER(u.nom) = LOWER(:searchTerm) THEN 1 " +
           "  WHEN LOWER(u.prenom) = LOWER(:searchTerm) THEN 2 " +
           "  WHEN LOWER(u.email) = LOWER(:searchTerm) THEN 3 " +
           "  WHEN LOWER(u.nom) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 4 " +
           "  WHEN LOWER(u.prenom) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 5 " +
           "  WHEN LOWER(u.email) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 6 " +
           "  ELSE 7 " +
           "END, u.nom ASC, u.prenom ASC")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
}
