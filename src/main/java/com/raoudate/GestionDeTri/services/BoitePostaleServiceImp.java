package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.Dto.BoitePostaleDTO;
import com.raoudate.GestionDeTri.Exception.BusinessErrorCode;
import com.raoudate.GestionDeTri.Exception.BusinessException;
import com.raoudate.GestionDeTri.model.Agences;
import com.raoudate.GestionDeTri.model.BoitePostale;
import com.raoudate.GestionDeTri.repository.AgenceRepository;
import com.raoudate.GestionDeTri.repository.BoitePostaleRepository;
import com.raoudate.GestionDeTri.services.api.BoitePostaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoitePostaleServiceImp implements BoitePostaleService {

    private final BoitePostaleRepository boitePostaleRepository;
    private final AgenceRepository agenceRepository;

    @Override
    @Transactional
    public BoitePostaleDTO save(BoitePostaleDTO dto) {
        log.info("Sauvegarde de la boîte postale: {}", dto.getLibelle());

        // Validation de l'agence
        if (dto.getAgenceId() == null) {
            throw new BusinessException(BusinessErrorCode.VALIDATION_ERROR, "L'ID de l'agence est obligatoire");
        }

        Agences agence = agenceRepository.findById(dto.getAgenceId())
                .orElseThrow(() -> new BusinessException(
                        BusinessErrorCode.AGENCE_NOT_FOUND,
                        "Agence non trouvée avec l'ID: " + dto.getAgenceId()
                ));

        BoitePostale boitePostale;
        if (dto.getId() != null) {
            boitePostale = boitePostaleRepository.findById(dto.getId())
                    .orElseThrow(() -> new BusinessException(
                            BusinessErrorCode.BOITE_POSTALE_NOT_FOUND,
                            "Boîte postale non trouvée avec l'ID: " + dto.getId()
                    ));
        } else {
            boitePostale = new BoitePostale();
        }

        // Mise à jour des propriétés
        boitePostale.setLibelle(dto.getLibelle());
        boitePostale.setCapacite(dto.getCapacite());
        boitePostale.setAgence(agence);

        BoitePostale savedBoitePostale = boitePostaleRepository.save(boitePostale);
        log.info("Boîte postale sauvegardée avec succès: {}", savedBoitePostale.getId());

        return BoitePostaleDTO.fromEntity(savedBoitePostale);
    }

    @Override
    @Transactional(readOnly = true)
    public BoitePostaleDTO findById(Integer id) {
        log.info("Recherche de la boîte postale avec l'ID: {}", id);
        return boitePostaleRepository.findById(id)
                .map(BoitePostaleDTO::fromEntity)
                .orElseThrow(() -> new BusinessException(
                        BusinessErrorCode.BOITE_POSTALE_NOT_FOUND,
                        "Boîte postale non trouvée avec l'ID: " + id
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoitePostaleDTO> findAll() {
        log.info("Récupération de toutes les boîtes postales");
        return boitePostaleRepository.findAllActive().stream()
                .map(BoitePostaleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BoitePostaleDTO> findAll(Pageable pageable) {
        log.info("Récupération des boîtes postales avec pagination");
        return boitePostaleRepository.findAll(pageable)
                .map(BoitePostaleDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoitePostaleDTO> findByAgenceId(Integer agenceId) {
        log.info("Récupération des boîtes postales de l'agence: {}", agenceId);
        return boitePostaleRepository.findByAgenceId(agenceId).stream()
                .map(BoitePostaleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BoitePostaleDTO> findByAgenceId(Integer agenceId, Pageable pageable) {
        log.info("Récupération des boîtes postales de l'agence {} avec pagination", agenceId);
        return boitePostaleRepository.findByAgenceId(agenceId, pageable)
                .map(BoitePostaleDTO::fromEntity);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.info("Suppression de la boîte postale avec l'ID: {}", id);
        BoitePostale boitePostale = boitePostaleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        BusinessErrorCode.BOITE_POSTALE_NOT_FOUND,
                        "Boîte postale non trouvée avec l'ID: " + id
                ));

        // Soft delete
        boitePostale.setDeletedAt(Instant.now());
        boitePostaleRepository.save(boitePostale);
        log.info("Boîte postale supprimée avec succès: {}", id);
    }
}
