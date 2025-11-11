package com.raoudate.GestionDeTri.scheduler;

import com.raoudate.GestionDeTri.model.Colis;
import com.raoudate.GestionDeTri.Enum.StatutColis;
import com.raoudate.GestionDeTri.repository.ColisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduler pour gérer le retour automatique des colis
 * après 30 jours de réception non réclamés
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ColisRetourScheduler {

    private static final int JOURS_AVANT_RETOUR = 30;
    private final ColisRepository colisRepository;

    /**
     * Vérifie et marque automatiquement comme RETOUR les colis
     * réceptionnés depuis plus de 30 jours et non livrés
     * S'exécute tous les jours à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void verifierEtRetournerColisNonReclames() {
        log.info("Début de la vérification des colis à retourner automatiquement...");
        
        try {
            Instant dateLimit = Instant.now().minus(JOURS_AVANT_RETOUR, ChronoUnit.DAYS);
            
            // Récupérer tous les colis réceptionnés depuis plus de 30 jours
            List<Colis> colisARetourner = colisRepository.findByStatutAndDateReceptionBefore(
                StatutColis.RECEPTIONNE, 
                dateLimit
            );
            
            if (colisARetourner.isEmpty()) {
                log.info("Aucun colis à retourner automatiquement");
                return;
            }
            
            int count = 0;
            for (Colis colis : colisARetourner) {
                // Calculer le nombre de jours depuis la réception
                long joursDepuisReception = ChronoUnit.DAYS.between(
                    colis.getDateReception(), 
                    Instant.now()
                );
                
                log.info("Retour automatique du colis {} - Réceptionné depuis {} jours", 
                    colis.getCodeSuivi(), 
                    joursDepuisReception);
                
                colis.setStatut(StatutColis.RETOUR);
                colis.setDateRetour(Instant.now());
                colisRepository.save(colis);
                count++;
            }
            
            log.info("Retour automatique terminé : {} colis marqués comme RETOUR", count);
            
        } catch (Exception e) {
            log.error("Erreur lors du retour automatique des colis : {}", e.getMessage(), e);
        }
    }
    
    /**
     * Récupère les colis qui seront bientôt retournés (dans les 7 jours)
     * Utile pour les alertes
     */
    public List<Colis> getColisProcheDuRetour() {
        Instant dateDebut = Instant.now().minus(JOURS_AVANT_RETOUR - 7, ChronoUnit.DAYS);
        Instant dateFin = Instant.now().minus(JOURS_AVANT_RETOUR, ChronoUnit.DAYS);
        
        return colisRepository.findByStatutAndDateReceptionBetween(
            StatutColis.RECEPTIONNE,
            dateFin,
            dateDebut
        );
    }
    
    /**
     * Calcule le nombre de jours restants avant retour automatique
     */
    public long getJoursRestantsAvantRetour(Colis colis) {
        if (colis.getStatut() != StatutColis.RECEPTIONNE || colis.getDateReception() == null) {
            return -1;
        }
        
        long joursDepuisReception = ChronoUnit.DAYS.between(
            colis.getDateReception(),
            Instant.now()
        );
        
        return JOURS_AVANT_RETOUR - joursDepuisReception;
    }
}
