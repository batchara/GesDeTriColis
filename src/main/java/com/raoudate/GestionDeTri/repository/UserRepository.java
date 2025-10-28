package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
