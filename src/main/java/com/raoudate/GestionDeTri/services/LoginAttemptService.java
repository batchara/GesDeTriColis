package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 *  Service de gestion des tentatives de connexion échouées
 * 
 * Fonctionnalités:
 * - Comptage des tentatives échouées
 * - Verrouillage automatique après 3 tentatives
 * - Réinitialisation du compteur après succès
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    
    private static final int MAX_ATTEMPTS = 3;
    private final UserRepository userRepository;
    
    /**
     * Enregistre une tentative de connexion échouée
     * Verrouille le compte si 3 tentatives échouées
     */
    @Transactional
    public void loginFailed(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            user.setLastFailedLogin(LocalDate.now());
            
            if (attempts >= MAX_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDate.now());
            }
            
            userRepository.save(user);
        });
    }
    
    /**
     * Réinitialise le compteur après une connexion réussie
     */
    @Transactional
    public void loginSucceeded(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                user.setLastFailedLogin(null);
                userRepository.save(user);
            }
        });
    }
    
    /**
     * Déverrouille manuellement un compte (par l'admin)
     */
    @Transactional
    public void unlockAccount(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setLockTime(null);
            user.setLastFailedLogin(null);
            userRepository.save(user);
        });
    }
    
    /**
     * Vérifie si un compte est verrouillé et retourne le nombre de tentatives
     */
    public int getRemainingAttempts(String email) {
        return userRepository.findByEmail(email)
            .map(user -> Math.max(0, MAX_ATTEMPTS - user.getFailedLoginAttempts()))
            .orElse(MAX_ATTEMPTS);
    }
}
