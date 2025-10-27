package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.Dto.ColisDTO;
import com.raoudate.GestionDeTri.Enum.StatutColis;
import com.raoudate.GestionDeTri.model.Colis;
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

    @Override
    public ColisDTO save(ColisDTO colisDTO) {
        log.info("Enregistrement d'un nouveau colis");
        
        Colis colis = ColisDTO.toEntity(colisDTO);
        
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
        log.info("Colis enregistré avec le code: {}", savedColis.getCodeSuivi());
        
        return ColisDTO.fromEntity(savedColis);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColisDTO> findAll() {
        log.info("Récupération de tous les colis");
        return colisRepository.findAll()
                .stream()
                .map(ColisDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ColisDTO update(Integer id, ColisDTO colisDTO) {
        log.info("Mise à jour du colis avec l'ID: {}", id);
        
        Colis existingColis = colisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colis non trouvé avec l'ID: " + id));
        
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
            existingColis.setStatut(colisDTO.getStatut());
        }
        
        Colis updatedColis = colisRepository.save(existingColis);
        log.info("Colis mis à jour: {}", updatedColis.getCodeSuivi());
        
        return ColisDTO.fromEntity(updatedColis);
    }

    @Override
    public void delete(Integer id) {
        log.info("Suppression du colis avec l'ID: {}", id);
        
        if (!colisRepository.existsById(id)) {
            throw new RuntimeException("Colis non trouvé avec l'ID: " + id);
        }
        
        colisRepository.deleteById(id);
        log.info("Colis supprimé avec succès");
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
