package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.Dto.AgenceDTO;
import com.raoudate.GestionDeTri.Exception.BusinessErrorCode;
import com.raoudate.GestionDeTri.Exception.BusinessException;
import com.raoudate.GestionDeTri.model.Agences;
import com.raoudate.GestionDeTri.repository.AgenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agences")
@RequiredArgsConstructor
public class AgenceController {

    private final AgenceRepository agenceRepository;

    /**
     * Récupère toutes les agences sans pagination (pour compatibilité)
     * Filtre automatiquement les agences supprimées
     */
    @GetMapping("/all")
    public ResponseEntity<List<AgenceDTO>> getAllAgences() {
        List<Agences> agences = agenceRepository.findAllActive();
        List<AgenceDTO> agenceDTOs = agences.stream()
                .map(AgenceDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(agenceDTOs);
    }

    /**
     * Récupère les agences avec pagination
     * @param page Numéro de la page (commence à 0)
     * @param size Nombre d'éléments par page (par défaut 10)
     * @param sortBy Champ de tri (par défaut "label")
     * @param direction Direction du tri (ASC ou DESC, par défaut ASC)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAgencesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "label") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<Agences> agencesPage = agenceRepository.findAll(pageable);
        
        List<AgenceDTO> agenceDTOs = agencesPage.getContent().stream()
                .map(AgenceDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("agences", agenceDTOs);
        response.put("currentPage", agencesPage.getNumber());
        response.put("totalItems", agencesPage.getTotalElements());
        response.put("totalPages", agencesPage.getTotalPages());
        response.put("pageSize", agencesPage.getSize());
        response.put("hasNext", agencesPage.hasNext());
        response.put("hasPrevious", agencesPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgenceDTO> getAgenceById(@PathVariable Integer id) {
        return agenceRepository.findById(id)
                .map(AgenceDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AgenceDTO> createAgence(@RequestBody AgenceDTO agenceDTO) {
        // Vérifier si l'agence existe déjà par code
        if (agenceDTO.getCode() != null && agenceRepository.findByCode(agenceDTO.getCode()).isPresent()) {
            throw new BusinessException(BusinessErrorCode.AGENCE_CODE_ALREADY_EXISTS);
        }
        
        // Vérifier si l'agence existe déjà par label (nom)
        if (agenceDTO.getNom() != null && agenceRepository.findByLabel(agenceDTO.getNom()).isPresent()) {
            throw new BusinessException(BusinessErrorCode.AGENCE_NAME_ALREADY_EXISTS);
        }
        
        Agences agence = AgenceDTO.toEntity(agenceDTO);
        Agences savedAgence = agenceRepository.save(agence);
        return ResponseEntity.ok(AgenceDTO.fromEntity(savedAgence));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<AgenceDTO> updateAgence(
            @PathVariable Integer id,
            @RequestBody AgenceDTO agenceDTO) {
        return agenceRepository.findById(id)
                .map(existingAgence -> {
                    // Vérifier si le code est modifié et s'il existe déjà pour une autre agence
                    if (agenceDTO.getCode() != null && !agenceDTO.getCode().equals(existingAgence.getCode())) {
                        agenceRepository.findByCode(agenceDTO.getCode()).ifPresent(agence -> {
                            if (!agence.getId().equals(id)) {
                                throw new BusinessException(BusinessErrorCode.AGENCE_CODE_ALREADY_EXISTS);
                            }
                        });
                    }
                    
                    // Vérifier si le nom (label) est modifié et s'il existe déjà pour une autre agence
                    if (agenceDTO.getNom() != null && !agenceDTO.getNom().equals(existingAgence.getLabel())) {
                        agenceRepository.findByLabel(agenceDTO.getNom()).ifPresent(agence -> {
                            if (!agence.getId().equals(id)) {
                                throw new BusinessException(BusinessErrorCode.AGENCE_NAME_ALREADY_EXISTS);
                            }
                        });
                    }
                    
                    existingAgence.setLabel(agenceDTO.getNom());
                    existingAgence.setCode(agenceDTO.getCode());
                    existingAgence.setEmail(agenceDTO.getEmail());
                    existingAgence.setTel(agenceDTO.getTel() != null ? agenceDTO.getTel() : agenceDTO.getNumTel());
                    existingAgence.setRegion(agenceDTO.getRegion());
                    existingAgence.setAdresseComplete(agenceDTO.getAdresseComplete());
                    existingAgence.setLatitude(agenceDTO.getLatitude());
                    existingAgence.setLongitude(agenceDTO.getLongitude());
                    if (agenceDTO.getStatus() != null) {
                        existingAgence.setStatus(agenceDTO.getStatus());
                    }
                    Agences updatedAgence = agenceRepository.save(existingAgence);
                    return ResponseEntity.ok(AgenceDTO.fromEntity(updatedAgence));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<Void> deleteAgence(@PathVariable Integer id) {
        return agenceRepository.findById(id)
                .map(agence -> {
                    // Soft delete : marquer l'agence comme supprimée au lieu de la supprimer physiquement
                    agence.setDeleted(true);
                    agence.setDeletedAt(java.time.Instant.now());
                    
                    // Récupérer l'utilisateur connecté pour traçabilité
                    org.springframework.security.core.Authentication authentication = 
                        org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.isAuthenticated()) {
                        agence.setDeletedBy(authentication.getName());
                    } else {
                        agence.setDeletedBy("SYSTEM");
                    }
                    
                    agenceRepository.save(agence);
                    System.out.println("✅ Agence marquée comme supprimée (soft delete): " + agence.getLabel());
                    System.out.println("📋 Supprimée par: " + agence.getDeletedBy() + " à " + agence.getDeletedAt());
                    
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Recherche paginée d'agences par région
     */
    @GetMapping("/search/region/{region}")
    public ResponseEntity<Map<String, Object>> searchAgencesByRegion(
            @PathVariable String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "label") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<Agences> agencesPage = agenceRepository.findByRegionContainingIgnoreCase(region, pageable);
        
        List<AgenceDTO> agenceDTOs = agencesPage.getContent().stream()
                .map(AgenceDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("agences", agenceDTOs);
        response.put("currentPage", agencesPage.getNumber());
        response.put("totalItems", agencesPage.getTotalElements());
        response.put("totalPages", agencesPage.getTotalPages());
        response.put("pageSize", agencesPage.getSize());
        response.put("hasNext", agencesPage.hasNext());
        response.put("hasPrevious", agencesPage.hasPrevious());
        response.put("searchTerm", region);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Recherche paginée d'agences par région, nom et/ou code
     * Utilise le paramètre 'keyword' pour chercher dans le nom OU le code
     * Utilise le paramètre 'region' pour filtrer par région
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchAgences(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "label") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        // Logs de débogage
        System.out.println("🔍 [AgenceController] Recherche d'agences avec paramètres:");
        System.out.println("  - keyword: " + keyword);
        System.out.println("  - region: " + region);
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<Agences> agencesPage;
        String searchType = "";
        
        // Si keyword ET région fournis
        if (keyword != null && !keyword.trim().isEmpty() && region != null && !region.trim().isEmpty()) {
            searchType = "Région + Keyword (nom OU code)";
            agencesPage = agenceRepository.findByRegionContainingIgnoreCaseAndLabelContainingIgnoreCaseOrRegionContainingIgnoreCaseAndCodeContainingIgnoreCase(
                    region, keyword, region, keyword, pageable);
        }
        // Si seulement keyword
        else if (keyword != null && !keyword.trim().isEmpty()) {
            searchType = "Keyword seul (cherche dans nom OU code)";
            agencesPage = agenceRepository.findByLabelContainingIgnoreCaseOrCodeContainingIgnoreCase(
                    keyword, keyword, pageable);
        }
        // Si seulement région
        else if (region != null && !region.trim().isEmpty()) {
            searchType = "Région seule";
            agencesPage = agenceRepository.findByRegionContainingIgnoreCase(region, pageable);
        }
        // Retourner toutes les agences
        else {
            searchType = "Toutes les agences";
            agencesPage = agenceRepository.findAll(pageable);
        }
        
        System.out.println("✅ [AgenceController] Type de recherche: " + searchType);
        System.out.println("📊 [AgenceController] Résultats trouvés: " + agencesPage.getTotalElements());
        
        List<AgenceDTO> agenceDTOs = agencesPage.getContent().stream()
                .map(AgenceDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("agences", agenceDTOs);
        response.put("currentPage", agencesPage.getNumber());
        response.put("totalItems", agencesPage.getTotalElements());
        response.put("totalPages", agencesPage.getTotalPages());
        response.put("pageSize", agencesPage.getSize());
        response.put("hasNext", agencesPage.hasNext());
        response.put("hasPrevious", agencesPage.hasPrevious());
        response.put("searchTerm", keyword);
        response.put("regionFilter", region);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/code/{code}")
    public ResponseEntity<Boolean> checkAgencyExistsByCode(@PathVariable String code) {
        boolean exists = agenceRepository.findByCode(code).isPresent();
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> checkAgencyExistsByName(@PathVariable String name) {
        boolean exists = agenceRepository.findByLabel(name).isPresent();
        return ResponseEntity.ok(exists);
    }
}
