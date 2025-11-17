package com.raoudate.GestionDeTri.services.api;

import com.raoudate.GestionDeTri.Dto.BoitePostaleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BoitePostaleService {

    /**
     * Crée ou met à jour une boîte postale
     */
    BoitePostaleDTO save(BoitePostaleDTO dto);

    /**
     * Récupère une boîte postale par son ID
     */
    BoitePostaleDTO findById(Integer id);

    /**
     * Récupère toutes les boîtes postales
     */
    List<BoitePostaleDTO> findAll();

    /**
     * Récupère toutes les boîtes postales avec pagination
     */
    Page<BoitePostaleDTO> findAll(Pageable pageable);

    /**
     * Récupère toutes les boîtes postales d'une agence
     */
    List<BoitePostaleDTO> findByAgenceId(Integer agenceId);

    /**
     * Récupère toutes les boîtes postales d'une agence avec pagination
     */
    Page<BoitePostaleDTO> findByAgenceId(Integer agenceId, Pageable pageable);

    /**
     * Supprime une boîte postale (soft delete)
     */
    void delete(Integer id);
}
