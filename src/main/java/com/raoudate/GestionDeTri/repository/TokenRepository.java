package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.Token;
import com.raoudate.GestionDeTri.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByToken(String token);

    Optional<Token> findTopByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT t FROM Token t WHERE t.user.id = :userId")
    java.util.List<Token> findAllByUserId(Integer userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Token t WHERE t.user = :user")
    void deleteAllByUser(User user);
}
