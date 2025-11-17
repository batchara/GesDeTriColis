package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.BoitePostale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoitePostaleRepository extends JpaRepository<BoitePostale, Integer> {

    /**
     * Trouve toutes les boîtes postales actives (non supprimées)
     */
    @Query("SELECT b FROM BoitePostale b WHERE b.deletedAt IS NULL")
    List<BoitePostale> findAllActive();

    /**
     * Trouve toutes les boîtes postales d'une agence
     */
    @Query("SELECT b FROM BoitePostale b WHERE b.agence.id = :agenceId AND b.deletedAt IS NULL")
    List<BoitePostale> findByAgenceId(@Param("agenceId") Integer agenceId);

    /**
     * Trouve toutes les boîtes postales d'une agence avec pagination
     */
    @Query("SELECT b FROM BoitePostale b WHERE b.agence.id = :agenceId AND b.deletedAt IS NULL")
    Page<BoitePostale> findByAgenceId(@Param("agenceId") Integer agenceId, Pageable pageable);


}
