package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.Dto.HomeStatsDTO;
import com.raoudate.GestionDeTri.repository.AgenceRepository;
import com.raoudate.GestionDeTri.repository.ColisRepository;
import com.raoudate.GestionDeTri.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final ColisRepository colisRepository;
    private final AgenceRepository agenceRepository;
    private final UserRepository userRepository;

    /**
     * Récupérer les statistiques pour la page d'accueil
     * Les données sont mises en cache pour optimiser les performances
     */
    @GetMapping("/stats")
    @Cacheable("homeStats")
    public ResponseEntity<HomeStatsDTO> getHomeStats() {
        HomeStatsDTO stats = HomeStatsDTO.builder()
                .totalColis(colisRepository.count())
                .colisEnAttente(colisRepository.countByStatut("EN_ATTENTE"))
                .colisLivres(colisRepository.countByStatut("LIVRE"))
                .totalAgences(agenceRepository.count())
                .totalUtilisateurs(userRepository.count())
                .build();
        
        return ResponseEntity.ok(stats);
    }
}
