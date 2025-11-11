package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.Dto.ColisDTO;
import com.raoudate.GestionDeTri.model.Colis;
import com.raoudate.GestionDeTri.scheduler.ColisRetourScheduler;
import com.raoudate.GestionDeTri.services.api.ColisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/colis")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ColisController {

    private final ColisService colisService;
    private final ColisRetourScheduler colisRetourScheduler;

    /**
     * Récupérer tous les colis
     * Accessible par Admin, Superviseur et Opérateur
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'OPERATEUR')")
    public ResponseEntity<List<ColisDTO>> getAllColis() {
        log.info("Récupération de tous les colis");
        List<ColisDTO> colisList = colisService.findAll();
        return ResponseEntity.ok(colisList);
    }

    /**
     * Créer un nouveau colis
     * Accessible par Admin, Superviseur et Opérateur
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'OPERATEUR')")
    public ResponseEntity<ColisDTO> createColis(@RequestBody ColisDTO colisDTO) {
        log.info("Création d'un nouveau colis pour destinataire: {}", colisDTO.getNomDest());
        try {
            ColisDTO savedColis = colisService.save(colisDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedColis);
        } catch (Exception e) {
            log.error("Erreur lors de la création du colis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mettre à jour un colis existant
     * Accessible par Admin, Superviseur et Opérateur
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'OPERATEUR')")
    public ResponseEntity<ColisDTO> updateColis(
            @PathVariable Integer id,
            @RequestBody ColisDTO colisDTO) {
        log.info("Mise à jour du colis ID: {}", id);
        try {
            ColisDTO updatedColis = colisService.update(id, colisDTO);
            return ResponseEntity.ok(updatedColis);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour du colis", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprimer un colis
     * Accessible uniquement par Admin
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteColis(@PathVariable Integer id) {
        log.info("Suppression du colis ID: {}", id);
        try {
            colisService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression du colis", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupérer les colis proches du retour automatique (dans les 7 prochains jours)
     * Accessible par Admin et Superviseur
     */
    @GetMapping("/proche-retour")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<List<Map<String, Object>>> getColisProcheDuRetour() {
        log.info("Récupération des colis proches du retour automatique");
        List<Colis> colisList = colisRetourScheduler.getColisProcheDuRetour();
        
        List<Map<String, Object>> response = colisList.stream()
            .map(colis -> {
                Map<String, Object> colisInfo = new HashMap<>();
                colisInfo.put("colis", ColisDTO.fromEntity(colis));
                colisInfo.put("joursRestants", colisRetourScheduler.getJoursRestantsAvantRetour(colis));
                return colisInfo;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Calculer les jours restants avant retour automatique pour un colis
     * Accessible par tous les rôles
     */
    @GetMapping("/{id}/jours-restants-retour")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR', 'OPERATEUR')")
    public ResponseEntity<Map<String, Object>> getJoursRestantsRetour(@PathVariable Integer id) {
        log.info("Calcul des jours restants avant retour pour le colis ID: {}", id);
        try {
            ColisDTO colisDTO = colisService.findById(id);
            Colis colis = ColisDTO.toEntity(colisDTO);
            long joursRestants = colisRetourScheduler.getJoursRestantsAvantRetour(colis);
            
            Map<String, Object> response = new HashMap<>();
            response.put("colisId", id);
            response.put("codeSuivi", colis.getCodeSuivi());
            response.put("statut", colis.getStatut());
            response.put("joursRestants", joursRestants);
            response.put("dateReception", colis.getDateReception());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors du calcul des jours restants", e);
            return ResponseEntity.notFound().build();
        }
    }
}
