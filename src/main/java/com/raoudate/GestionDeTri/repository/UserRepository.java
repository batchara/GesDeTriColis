package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByNom(String nom);

    Optional<User> findByEmail(String email);
}
