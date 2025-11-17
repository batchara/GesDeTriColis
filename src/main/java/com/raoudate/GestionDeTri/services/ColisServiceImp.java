package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.Dto.ColisDTO;
import com.raoudate.GestionDeTri.Enum.StatutColis;
import com.raoudate.GestionDeTri.model.Agences;
import com.raoudate.GestionDeTri.model.Colis;
import com.raoudate.GestionDeTri.repository.AgenceRepository;
import com.raoudate.GestionDeTri.repository.ColisRepository;
import com.raoudate.GestionDeTri.services.api.ColisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ColisServiceImp implements ColisService {

    private final ColisRepository colisRepository;
    private final AgenceRepository agenceRepository;

    @Override
    public ColisDTO save(ColisDTO colisDTO) {
        log.info("Enregistrement d'un nouveau colis");
        
        Colis colis = ColisDTO.toEntity(colisDTO);
        
        // ✅ Vérifier si un colis avec ce code de suivi existe déjà
        if (colis.getCodeSuivi() != null && !colis.getCodeSuivi().isEmpty()) {
            boolean exists = colisRepository.existsByCodeSuivi(colis.getCodeSuivi());
            if (exists) {
                log.error("❌ Un colis avec le code de suivi {} existe déjà", colis.getCodeSuivi());
                throw new RuntimeException("Un colis avec le code de suivi " + colis.getCodeSuivi() + " existe déjà dans le système");
            }
        }
        
        // ✅ Gérer la relation agenceAffectee : récupérer l'agence depuis la base de données
        if (colisDTO.getAgenceAffectee() != null && colisDTO.getAgenceAffectee().getId() != null) {
            Integer agenceId = colisDTO.getAgenceAffectee().getId();
            log.info("Récupération de l'agence avec ID: {}", agenceId);
            
            Agences agence = agenceRepository.findById(agenceId)
                    .orElseThrow(() -> new RuntimeException("Agence non trouvée avec l'ID: " + agenceId));
            
            colis.setAgenceAffectee(agence);
            log.info("Agence affectée au colis: {} (code: {})", agence.getLabel(), agence.getCode());
        }
        
        // Générer un code de suivi unique si non fourni
        if (colis.getCodeSuivi() == null || colis.getCodeSuivi().isEmpty()) {
            colis.setCodeSuivi(generateCodeSuivi());
        }
        
        // Définir la date d'envoi si non fournie
        if (colis.getDateEnvoi() == null) {
            colis.setDateEnvoi(Instant.now());
        }
        
        // Définir le statut par défaut si non fourni
        if (colis.getStatut() == null) {
            colis.setStatut(StatutColis.EN_ATTENTE);
        }
        
        Colis savedColis = colisRepository.save(colis);
        log.info("✅ Colis enregistré avec succès - Code: {}", savedColis.getCodeSuivi());
        
        return ColisDTO.fromEntity(savedColis);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColisDTO> findAll() {
        log.info("📋 Récupération de tous les colis NON supprimés");
        List<Colis> colisList = colisRepository.findAllActive();
        log.info("✅ {} colis actifs trouvés", colisList.size());
        
        return colisList.stream()
                .map(ColisDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ColisDTO findById(Integer id) {
        log.info("Récupération du colis avec l'ID: {}", id);
        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colis non trouvé avec l'ID: " + id));
        return ColisDTO.fromEntity(colis);
    }

    @Override
    public ColisDTO update(Integer id, ColisDTO colisDTO) {
        log.info("Mise à jour du colis avec l'ID: {}", id);
        
        Colis existingColis = colisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colis non trouvé avec l'ID: " + id));
        
        // Sauvegarder l'ancien statut pour détecter les changements
        StatutColis ancienStatut = existingColis.getStatut();
        
        // Mettre à jour les champs
        if (colisDTO.getPoids() != null) {
            existingColis.setPoids(colisDTO.getPoids());
        }
        if (colisDTO.getNomExp() != null) {
            existingColis.setNomExp(colisDTO.getNomExp());
        }
        if (colisDTO.getNomDest() != null) {
            existingColis.setNomDest(colisDTO.getNomDest());
        }
        if (colisDTO.getTelDest() != null) {
            existingColis.setTelDest(colisDTO.getTelDest());
        }
        if (colisDTO.getAdresseDest() != null) {
            existingColis.setAdresseDest(colisDTO.getAdresseDest());
        }
        if (colisDTO.getDatePrevue() != null) {
            existingColis.setDatePrevue(colisDTO.getDatePrevue());
        }
        if (colisDTO.getStatut() != null) {
            StatutColis nouveauStatut = colisDTO.getStatut();
            existingColis.setStatut(nouveauStatut);
            
            // Mettre à jour les dates selon le nouveau statut
            if (ancienStatut != nouveauStatut) {
                if (nouveauStatut == StatutColis.RECEPTIONNE && existingColis.getDateReception() == null) {
                    existingColis.setDateReception(Instant.now());
                    log.info("Date de réception mise à jour pour le colis: {}", existingColis.getCodeSuivi());
                } else if (nouveauStatut == StatutColis.RETOUR && existingColis.getDateRetour() == null) {
                    existingColis.setDateRetour(Instant.now());
                    log.info("Date de retour mise à jour pour le colis: {}", existingColis.getCodeSuivi());
                }
            }
        }
        
        Colis updatedColis = colisRepository.save(existingColis);
        log.info("Colis mis à jour: {}", updatedColis.getCodeSuivi());
        
        return ColisDTO.fromEntity(updatedColis);
    }

    @Override
    public void delete(Integer id) {
        log.info("🗑️ Demande de suppression du colis avec l'ID: {}", id);
        
        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Colis non trouvé avec l'ID: {}", id);
                    return new RuntimeException("Colis non trouvé avec l'ID: " + id);
                });
        
        log.info("📦 Colis trouvé - Code: {}, Deleted: {}", colis.getCodeSuivi(), colis.getDeleted());
        
        // Soft delete : marquer le colis comme supprimé au lieu de le supprimer physiquement
        colis.setDeleted(true);
        colis.setDeletedAt(java.time.Instant.now());
        
        // Récupérer l'utilisateur connecté pour traçabilité
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            colis.setDeletedBy(authentication.getName());
        } else {
            colis.setDeletedBy("SYSTEM");
        }
        
        colisRepository.save(colis);
        log.info("✅ Colis marqué comme supprimé (soft delete): {} - Supprimé par: {} à {}", 
                 colis.getCodeSuivi(), colis.getDeletedBy(), colis.getDeletedAt());
        
        // Vérification immédiate
        Colis verif = colisRepository.findById(id).orElse(null);
        if (verif != null) {
            log.info("🔍 Vérification: deleted={}, deletedAt={}, deletedBy={}", 
                     verif.getDeleted(), verif.getDeletedAt(), verif.getDeletedBy());
        }
    }
    
    @Override
    public int deleteMultiple(List<Integer> ids) {
        log.info("🗑️ Demande de suppression en masse de {} colis", ids.size());
        
        if (ids == null || ids.isEmpty()) {
            log.warn("⚠️ Aucun ID fourni pour la suppression en masse");
            return 0;
        }
        
        int successCount = 0;
        int errorCount = 0;
        
        // Récupérer l'utilisateur connecté une seule fois
        String deletedBy;
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            deletedBy = authentication.getName();
        } else {
            deletedBy = "SYSTEM";
        }
        
        Instant deletedAt = java.time.Instant.now();
        
        for (Integer id : ids) {
            try {
                Colis colis = colisRepository.findById(id).orElse(null);
                
                if (colis == null) {
                    log.warn("⚠️ Colis avec ID {} non trouvé - ignoré", id);
                    errorCount++;
                    continue;
                }
                
                if (colis.getDeleted() != null && colis.getDeleted()) {
                    log.warn("⚠️ Colis {} (ID: {}) est déjà supprimé - ignoré", colis.getCodeSuivi(), id);
                    errorCount++;
                    continue;
                }
                
                log.info("📦 Suppression du colis: {} (ID: {})", colis.getCodeSuivi(), id);
                
                // Soft delete
                colis.setDeleted(true);
                colis.setDeletedAt(deletedAt);
                colis.setDeletedBy(deletedBy);
                
                colisRepository.save(colis);
                successCount++;
                
            } catch (Exception e) {
                log.error("❌ Erreur lors de la suppression du colis ID {}: {}", id, e.getMessage());
                errorCount++;
            }
        }
        
        log.info("✅ Suppression en masse terminée: {} colis supprimés avec succès, {} erreurs", 
                 successCount, errorCount);
        
        return successCount;
    }
    
    /**
     * Génère un code de suivi unique pour un colis
     * Format: COL-YYYYMMDD-XXXXX
     */
    private String generateCodeSuivi() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "COL-" + timestamp.substring(timestamp.length() - 8) + "-" + uuid;
    }
}
