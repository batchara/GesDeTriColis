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
@RequestMapping("/colis")
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
    public ResponseEntity<?> createColis(@RequestBody ColisDTO colisDTO) {
        log.info("Création d'un nouveau colis pour destinataire: {}", colisDTO.getNomDest());
        try {
            ColisDTO savedColis = colisService.save(colisDTO);
            log.info("✅ Colis créé avec succès: {}", savedColis.getCodeSuivi());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedColis);
        } catch (RuntimeException e) {
            log.error("❌ Erreur métier lors de la création du colis: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur de validation");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "BAD_REQUEST");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("❌ Erreur technique lors de la création du colis", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur serveur");
            errorResponse.put("message", "Une erreur technique est survenue. Veuillez réessayer.");
            errorResponse.put("status", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
     * Accessible par Admin et Superviseur
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
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
     * Supprimer plusieurs colis en masse (soft delete)
     * Accessible par Admin et Superviseur
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    public ResponseEntity<Map<String, Object>> deleteMultipleColis(@RequestBody List<Integer> ids) {
        log.info("🗑️ Demande de suppression en masse de {} colis", ids.size());
        try {
            int deletedCount = colisService.deleteMultiple(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalRequested", ids.size());
            response.put("deletedCount", deletedCount);
            response.put("errorCount", ids.size() - deletedCount);
            response.put("message", deletedCount + " colis supprimé(s) avec succès");
            
            log.info("✅ Suppression en masse terminée: {} colis sur {} supprimés", deletedCount, ids.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la suppression en masse", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la suppression en masse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
            Colis colis = colisService.findEntityById(id);
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
