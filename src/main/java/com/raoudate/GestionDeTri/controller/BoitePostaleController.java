package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.Dto.BoitePostaleDTO;
import com.raoudate.GestionDeTri.services.api.BoitePostaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boites-postales")
@RequiredArgsConstructor
@Tag(name = "Boîtes Postales", description = "API de gestion des boîtes postales")
public class BoitePostaleController {

    private final BoitePostaleService boitePostaleService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Créer une nouvelle boîte postale", description = "Crée une nouvelle boîte postale dans une agence")
    @ApiResponse(responseCode = "201", description = "Boîte postale créée avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    public ResponseEntity<BoitePostaleDTO> createBoitePostale(@Valid @RequestBody BoitePostaleDTO boitePostaleDTO) {
        BoitePostaleDTO savedBoitePostale = boitePostaleService.save(boitePostaleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBoitePostale);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Mettre à jour une boîte postale", description = "Met à jour les informations d'une boîte postale existante")
    @ApiResponse(responseCode = "200", description = "Boîte postale mise à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Boîte postale non trouvée")
    public ResponseEntity<BoitePostaleDTO> updateBoitePostale(
            @PathVariable Integer id,
            @Valid @RequestBody BoitePostaleDTO boitePostaleDTO) {
        boitePostaleDTO.setId(id);
        BoitePostaleDTO updatedBoitePostale = boitePostaleService.save(boitePostaleDTO);
        return ResponseEntity.ok(updatedBoitePostale);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une boîte postale par ID", description = "Récupère les détails d'une boîte postale spécifique")
    @ApiResponse(responseCode = "200", description = "Boîte postale trouvée")
    @ApiResponse(responseCode = "404", description = "Boîte postale non trouvée")
    public ResponseEntity<BoitePostaleDTO> getBoitePostaleById(@PathVariable Integer id) {
        BoitePostaleDTO boitePostale = boitePostaleService.findById(id);
        return ResponseEntity.ok(boitePostale);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les boîtes postales avec pagination", description = "Récupère la liste paginée de toutes les boîtes postales")
    public ResponseEntity<Map<String, Object>> getAllBoitesPostales(
            @Parameter(description = "Numéro de la page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "numero") String sortBy,
            @Parameter(description = "Direction du tri (ASC ou DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<BoitePostaleDTO> boitesPostalesPage = boitePostaleService.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("boitesPostales", boitesPostalesPage.getContent());
        response.put("currentPage", boitesPostalesPage.getNumber());
        response.put("totalItems", boitesPostalesPage.getTotalElements());
        response.put("totalPages", boitesPostalesPage.getTotalPages());
        response.put("pageSize", boitesPostalesPage.getSize());
        response.put("hasNext", boitesPostalesPage.hasNext());
        response.put("hasPrevious", boitesPostalesPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Lister toutes les boîtes postales", description = "Récupère la liste de toutes les boîtes postales sans pagination")
    public ResponseEntity<List<BoitePostaleDTO>> getAllBoitesPostalesNoPagination() {
        List<BoitePostaleDTO> boitesPostales = boitePostaleService.findAll();
        return ResponseEntity.ok(boitesPostales);
    }

    @GetMapping("/agence/{agenceId}")
    @Operation(summary = "Lister les boîtes postales d'une agence", description = "Récupère toutes les boîtes postales d'une agence spécifique")
    public ResponseEntity<List<BoitePostaleDTO>> getBoitesPostalesByAgence(@PathVariable Integer agenceId) {
        List<BoitePostaleDTO> boitesPostales = boitePostaleService.findByAgenceId(agenceId);
        return ResponseEntity.ok(boitesPostales);
    }

    @GetMapping("/agence/{agenceId}/paginated")
    @Operation(summary = "Lister les boîtes postales d'une agence avec pagination", description = "Récupère les boîtes postales d'une agence avec pagination")
    public ResponseEntity<Map<String, Object>> getBoitesPostalesByAgencePaginated(
            @PathVariable Integer agenceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "numero") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<BoitePostaleDTO> boitesPostalesPage = boitePostaleService.findByAgenceId(agenceId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("boitesPostales", boitesPostalesPage.getContent());
        response.put("currentPage", boitesPostalesPage.getNumber());
        response.put("totalItems", boitesPostalesPage.getTotalElements());
        response.put("totalPages", boitesPostalesPage.getTotalPages());
        response.put("pageSize", boitesPostalesPage.getSize());
        response.put("hasNext", boitesPostalesPage.hasNext());
        response.put("hasPrevious", boitesPostalesPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Supprimer une boîte postale", description = "Supprime une boîte postale (soft delete)")
    @ApiResponse(responseCode = "204", description = "Boîte postale supprimée avec succès")
    @ApiResponse(responseCode = "404", description = "Boîte postale non trouvée")
    public ResponseEntity<Void> deleteBoitePostale(@PathVariable Integer id) {
        boitePostaleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
