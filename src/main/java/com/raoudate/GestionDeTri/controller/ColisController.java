package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.Dto.ColisDTO;
import com.raoudate.GestionDeTri.services.api.ColisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colis")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ColisController {

    private final ColisService colisService;

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
}
