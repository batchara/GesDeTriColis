package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.dto.response.AgenceDTO;
import com.raoudate.GestionDeTri.exception.BusinessErrorCode;
import com.raoudate.GestionDeTri.exception.BusinessException;
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
import java.util.Optional;
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
                    if (agenceDTO.getCodeBureau() != null) {
                        existingAgence.setCodeBureau(agenceDTO.getCodeBureau());
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
                    System.out.println(" Agence marquée comme supprimée (soft delete): " + agence.getLabel());
                    System.out.println(" Supprimée par: " + agence.getDeletedBy() + " à " + agence.getDeletedAt());
                    
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
        System.out.println(" [AgenceController] Recherche d'agences avec paramètres:");
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
        
        System.out.println(" [AgenceController] Type de recherche: " + searchType);
        System.out.println(" [AgenceController] Résultats trouvés: " + agencesPage.getTotalElements());
        
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

    /**
     * Récupère une agence par son Code_Bureau
     * Stratégie de recherche :
     * 1. Cherche par code_bureau exact
     * 2. Extrait les chiffres avant "BP" (ex: "01" de "01BP470")
     * 3. Cherche une agence dont le code commence par ces chiffres
     */
    @GetMapping("/by-code-bureau/{codeBureau}")
    public ResponseEntity<AgenceDTO> getAgenceByCodeBureau(@PathVariable String codeBureau) {
        String normalizedCode = codeBureau.trim().toUpperCase();
        
        //  Correction OCR: remplacer les caractères mal reconnus
        // IMPORTANT: Ne remplacer que O/l/I (lett lettres), pas le chiffre 1
        normalizedCode = normalizedCode.replaceAll("^[Ol](?=\\d)", "0"); // l/O au début -> 0 si suivi de chiffre (pas "1"!)
        normalizedCode = normalizedCode.replaceAll("^I(?=[BP])", "1"); // I au début -> 1 si suivi de B ou P
        normalizedCode = normalizedCode.replaceAll("([BP])l$", "$10"); // l à la fin -> 0
        
        System.out.println(" Recherche agence par codeBureau: " + normalizedCode + " (original: " + codeBureau + ")");
        
        // 1⃣ Essayer une correspondance exacte du codeBureau d'abord
        Optional<Agences> result = agenceRepository.findByCodeBureau(normalizedCode);
        System.out.println(" Recherche exacte: " + (result.isPresent() ? result.get().getNom() : "non trouvée"));
        
        // 2⃣ Si pas trouvé et le format est valide (ex: 01BP479), extraire le préfixe et chercher exactement
        if (result.isEmpty() && normalizedCode.contains("BP")) {
            String[] parts = normalizedCode.split("BP");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                String codePrefixRaw = parts[0]; // Ex: "01" de "01BP479"
                
                // SÉCURITÉ: Rejeter les préfixes trop courts (moins de 2 chiffres)
                // Si le préfixe est "1", c'est probablement une erreur OCR (devrait être "01")
                if (codePrefixRaw.length() < 2) {
                    System.out.println("  Préfixe trop court (" + codePrefixRaw + "), probablement une erreur OCR. Correction en cours...");
                    if (codePrefixRaw.equals("1")) {
                        // Essayer avec le préfixe "01" au lieu de "1"
                        final String codePrefix = "01";
                        System.out.println(" Correction appliquée: 01 (au lieu de " + codePrefixRaw + ")");
                        
                        List<Agences> allAgencies = agenceRepository.findAllActive();
                        System.out.println(" Total agences actives: " + allAgencies.size());
                        
                        // Chercher UNE agence avec codeBureau EXACTEMENT égal au préfixe "01"
                        result = allAgencies.stream()
                            .filter(a -> a.getCodeBureau() != null && 
                                   a.getCodeBureau().toUpperCase().equals(codePrefix))
                            .peek(a -> System.out.println(" Trouvée (étape 2 - exact): " + a.getNom() + " - codeBureau: " + a.getCodeBureau()))
                            .findFirst();
                        
                        // Fallback: chercher par code d'agence
                        if (result.isEmpty()) {
                            result = allAgencies.stream()
                                .filter(a -> a.getCode() != null && a.getCode().equals(codePrefix))
                                .peek(a -> System.out.println(" Trouvée (étape 3 - code): " + a.getNom() + " - code: " + a.getCode()))
                                .findFirst();
                        }
                    }
                } else {
                    // Préfixe normal, utiliser la recherche standard
                    final String codePrefix = codePrefixRaw;
                    System.out.println(" Extraction du préfixe: " + codePrefix);
                    
                    List<Agences> allAgencies = agenceRepository.findAllActive();
                    System.out.println(" Total agences actives: " + allAgencies.size());
                    
                    // Chercher UNE agence avec codeBureau EXACTEMENT égal au préfixe (ex: codeBureau = "01")
                    result = allAgencies.stream()
                        .filter(a -> a.getCodeBureau() != null && 
                               a.getCodeBureau().toUpperCase().equals(codePrefix))
                        .peek(a -> System.out.println(" Trouvée (étape 2 - exact): " + a.getNom() + " - codeBureau: " + a.getCodeBureau()))
                        .findFirst();
                    
                    // 3⃣ Si toujours pas trouvé, chercher par le code d'agence (pas startsWith sur codeBureau!)
                    if (result.isEmpty()) {
                        System.out.println(" Étape 2 échouée, essai étape 3 (match par code d'agence)");
                        result = allAgencies.stream()
                            .filter(a -> a.getCode() != null && a.getCode().equals(codePrefix))
                            .peek(a -> System.out.println(" Trouvée (étape 3 - code): " + a.getNom() + " - code: " + a.getCode()))
                            .findFirst();
                    }
                }
            }
        }
        
        if (result.isEmpty()) {
            System.out.println(" AUCUNE AGENCE TROUVÉE pour: " + normalizedCode);
        } else {
            System.out.println(" AGENCE TROUVÉE: " + result.get().getNom());
        }
        
        return result
                .map(AgenceDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

