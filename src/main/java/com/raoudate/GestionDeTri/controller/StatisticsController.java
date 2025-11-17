package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.Enum.StatutColis;
import com.raoudate.GestionDeTri.model.Agences;
import com.raoudate.GestionDeTri.repository.AgenceRepository;
import com.raoudate.GestionDeTri.repository.ColisRepository;
import com.raoudate.GestionDeTri.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {

    private final ColisRepository colisRepository;
    private final AgenceRepository agenceRepository;
    private final UserRepository userRepository;

    /**
     * Récupère les statistiques complètes du dashboard
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("📊 Récupération des statistiques du dashboard");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Statistiques générales
        long totalUsers = userRepository.count();
        long totalAgencies = agenceRepository.count();
        long totalColis = colisRepository.count();
        
        stats.put("totalUsers", totalUsers);
        stats.put("totalAgencies", totalAgencies);
        stats.put("totalColis", totalColis);
        
        // Statistiques des colis par statut
        Map<String, Long> colisParStatut = new HashMap<>();
        for (StatutColis statut : StatutColis.values()) {
            long count = colisRepository.countByStatutEnum(statut);
            colisParStatut.put(statut.name(), count);
        }
        stats.put("colisParStatut", colisParStatut);
        
        // Statistiques journalières (aujourd'hui)
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        long colisTraitesAujourdhui = colisRepository.countByDateEnvoiBetween(startOfDay, endOfDay);
        long colisLivresAujourdhui = colisRepository.countByStatutAndDateEnvoiBetween(
            StatutColis.LIVRE, startOfDay, endOfDay
        );
        long colisEnRetourAujourdhui = colisRepository.countByStatutAndDateEnvoiBetween(
            StatutColis.RETOUR, startOfDay, endOfDay
        );
        
        stats.put("colisTraitesAujourdhui", colisTraitesAujourdhui);
        stats.put("colisLivresAujourdhui", colisLivresAujourdhui);
        stats.put("colisEnRetourAujourdhui", colisEnRetourAujourdhui);
        
        // Agence la plus active
        List<Object[]> agencesActives = colisRepository.findTopAgencesByColisCount();
        Map<String, Object> agenceLaPlusActive = null;
        
        if (!agencesActives.isEmpty()) {
            Object[] topAgence = agencesActives.get(0);
            Agences agence = (Agences) topAgence[0];
            Long nombreColis = (Long) topAgence[1];
            
            agenceLaPlusActive = new HashMap<>();
            agenceLaPlusActive.put("id", agence.getId());
            agenceLaPlusActive.put("nom", agence.getLabel());
            agenceLaPlusActive.put("region", agence.getRegion());
            agenceLaPlusActive.put("nombreColis", nombreColis);
        }
        stats.put("agenceLaPlusActive", agenceLaPlusActive);
        
        // Top 5 agences actives
        List<Map<String, Object>> topAgencesActives = agencesActives.stream()
            .limit(5)
            .map(obj -> {
                Agences agence = (Agences) obj[0];
                Long nombreColis = (Long) obj[1];
                
                Map<String, Object> agenceMap = new HashMap<>();
                agenceMap.put("id", agence.getId());
                agenceMap.put("nom", agence.getLabel());
                agenceMap.put("region", agence.getRegion());
                agenceMap.put("nombreColis", nombreColis);
                return agenceMap;
            })
            .collect(Collectors.toList());
        stats.put("topAgencesActives", topAgencesActives);
        
        // Colis par jour (7 derniers jours)
        List<Map<String, Object>> colisParJour = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Instant start = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            
            long nombre = colisRepository.countByDateEnvoiBetween(start, end);
            
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("date", date.toString());
            dayStats.put("nombre", nombre);
            colisParJour.add(dayStats);
        }
        stats.put("colisParJour", colisParJour);
        
        // Statistiques utilisateurs
        long utilisateursActifs = userRepository.countByEnabledTrue();
        long utilisateursBloques = userRepository.countByAccountLockedTrue();
        
        stats.put("utilisateursActifs", utilisateursActifs);
        stats.put("utilisateursBloques", utilisateursBloques);
        
        // Dernières connexions (si vous avez ce champ)
        List<Map<String, Object>> dernieresConnexions = new ArrayList<>();
        // TODO: Implémenter si vous avez un champ lastLogin
        stats.put("dernieresConnexions", dernieresConnexions);
        
        // Performance du système
        double tauxReussite = totalColis > 0 
            ? (colisParStatut.getOrDefault("LIVRE", 0L) * 100.0) / totalColis 
            : 0.0;
        stats.put("tauxReussite", Math.round(tauxReussite * 100.0) / 100.0);
        
        // Temps de traitement moyen (exemple simplifié)
        stats.put("tempsTraitementMoyen", 24.5); // TODO: Calculer réellement
        
        // Statistiques par région pour le mois en cours
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        Instant startOfMonth = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfMonth = lastDayOfMonth.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        List<Object[]> colisParRegionMois = colisRepository.findColisCountByRegionAndPeriod(startOfMonth, endOfMonth);
        Map<String, Long> colisParRegionCeMois = colisParRegionMois.stream()
            .collect(Collectors.toMap(
                obj -> (String) obj[0] != null ? (String) obj[0] : "Non défini",
                obj -> (Long) obj[1]
            ));
        stats.put("colisParRegionCeMois", colisParRegionCeMois);
        
        log.info("✅ Statistiques récupérées avec succès");
        return ResponseEntity.ok(stats);
    }

    /**
     * Récupère les métriques en temps réel
     */
    @GetMapping("/realtime")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<Map<String, Object>> getRealTimeMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Timestamp actuel
        metrics.put("timestamp", Instant.now().toString());
        
        // Colis en cours (ni livrés ni annulés)
        long colisEnCours = colisRepository.countByStatutNotIn(
            Arrays.asList(StatutColis.LIVRE, StatutColis.RETOUR)
        );
        metrics.put("colisEnCours", colisEnCours);
        
        // Colis traités aujourd'hui
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        long colisTraitesAujourdhui = colisRepository.countByDateEnvoiBetween(startOfDay, endOfDay);
        metrics.put("colisTraitesAujourdhui", colisTraitesAujourdhui);
        
        // Utilisateurs connectés (simplifié - vous pourriez tracker les sessions actives)
        long utilisateursConnectes = userRepository.countByEnabledTrue();
        metrics.put("utilisateursConnectes", utilisateursConnectes);
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Récupère les statistiques des colis par période
     */
    @GetMapping("/colis/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<Map<String, Object>> getColisStatsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        Map<String, Object> stats = new HashMap<>();
        
        // Total colis dans la période
        long total = colisRepository.countByDateEnvoiBetween(start, end);
        stats.put("total", total);
        
        // Par statut
        Map<String, Long> parStatut = new HashMap<>();
        for (StatutColis statut : StatutColis.values()) {
            long count = colisRepository.countByStatutAndDateEnvoiBetween(statut, start, end);
            parStatut.put(statut.name(), count);
        }
        stats.put("parStatut", parStatut);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Récupère les statistiques d'une agence spécifique
     */
    @GetMapping("/agency/{agencyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<Map<String, Object>> getAgencyStats(@PathVariable Integer agencyId) {
        Map<String, Object> stats = new HashMap<>();
        
        if (agencyId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Vérifier que l'agence existe
        Optional<Agences> agenceOpt = agenceRepository.findById(agencyId);
        if (agenceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Agences agence = agenceOpt.get();
        stats.put("agence", Map.of(
            "id", agence.getId(),
            "nom", agence.getLabel(),
            "region", agence.getRegion()
        ));
        
        // Total colis de cette agence
        long totalColis = colisRepository.countByAgenceAffectee(agence);
        stats.put("totalColis", totalColis);
        
        // Par statut
        Map<String, Long> parStatut = new HashMap<>();
        for (StatutColis statut : StatutColis.values()) {
            long count = colisRepository.countByAgenceAffecteeAndStatut(agence, statut);
            parStatut.put(statut.name(), count);
        }
        stats.put("parStatut", parStatut);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Récupère les statistiques par région
     */
    @GetMapping("/regions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<List<Map<String, Object>>> getStatsByRegion() {
        List<Object[]> regionStats = colisRepository.findColisCountByRegion();
        
        List<Map<String, Object>> stats = regionStats.stream()
            .map(obj -> {
                String region = (String) obj[0];
                Long count = (Long) obj[1];
                
                Map<String, Object> regionMap = new HashMap<>();
                regionMap.put("region", region != null ? region : "Non défini");
                regionMap.put("nombreColis", count);
                return regionMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(stats);
    }
}
