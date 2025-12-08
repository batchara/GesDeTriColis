package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.Agences;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgenceRepository extends JpaRepository<Agences, Integer> {

    
    
    @Query("SELECT a FROM Agences a WHERE a.label = :label AND a.deleted = false")
    Optional<Agences> findByLabel(@Param("label") String label);

    @Query("SELECT a FROM Agences a WHERE a.code = :code AND a.deleted = false")
    Optional<Agences> findByCode(@Param("code") String code);
    
    @Query("SELECT a FROM Agences a WHERE a.codeBureau = :codeBureau AND a.deleted = false")
    Optional<Agences> findByCodeBureau(@Param("codeBureau") String codeBureau);
    
    /**
     * Récupère toutes les agences non supprimées
     */
    @Query("SELECT a FROM Agences a WHERE a.deleted = false")
    List<Agences> findAllActive();
    
    /**
     * Récupère une agence non supprimée par son ID
     */
    @Query("SELECT a FROM Agences a WHERE a.id = :id AND a.deleted = false")
    Optional<Agences> findActiveById(@Param("id") Integer id);
    
    
    
    @Query("SELECT a FROM Agences a WHERE a.region LIKE %:region% AND a.deleted = false")
    Page<Agences> findByRegionContainingIgnoreCase(@Param("region") String region, Pageable pageable);
    
    @Query("SELECT a FROM Agences a WHERE LOWER(a.label) LIKE LOWER(CONCAT('%', :label, '%')) AND a.deleted = false")
    Page<Agences> findByLabelContainingIgnoreCase(@Param("label") String label, Pageable pageable);
    
    @Query("SELECT a FROM Agences a WHERE LOWER(a.code) LIKE LOWER(CONCAT('%', :code, '%')) AND a.deleted = false")
    Page<Agences> findByCodeContainingIgnoreCase(@Param("code") String code, Pageable pageable);
    
    @Query("SELECT a FROM Agences a WHERE (LOWER(a.label) LIKE LOWER(CONCAT('%', :label, '%')) OR LOWER(a.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND a.deleted = false")
    Page<Agences> findByLabelContainingIgnoreCaseOrCodeContainingIgnoreCase(
            @Param("label") String label, @Param("code") String code, Pageable pageable);
    
    // Recherche combinée : région + (nom OU code)
    @Query("SELECT a FROM Agences a WHERE LOWER(a.region) LIKE LOWER(CONCAT('%', :region, '%')) AND LOWER(a.label) LIKE LOWER(CONCAT('%', :label, '%')) AND a.deleted = false")
    Page<Agences> findByRegionContainingIgnoreCaseAndLabelContainingIgnoreCase(
            @Param("region") String region, @Param("label") String label, Pageable pageable);
    
    @Query("SELECT a FROM Agences a WHERE LOWER(a.region) LIKE LOWER(CONCAT('%', :region, '%')) AND LOWER(a.code) LIKE LOWER(CONCAT('%', :code, '%')) AND a.deleted = false")
    Page<Agences> findByRegionContainingIgnoreCaseAndCodeContainingIgnoreCase(
            @Param("region") String region, @Param("code") String code, Pageable pageable);
    
    @Query("SELECT a FROM Agences a WHERE (LOWER(a.region) LIKE LOWER(CONCAT('%', :region1, '%')) AND LOWER(a.label) LIKE LOWER(CONCAT('%', :label, '%'))) OR (LOWER(a.region) LIKE LOWER(CONCAT('%', :region2, '%')) AND LOWER(a.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND a.deleted = false")
    Page<Agences> findByRegionContainingIgnoreCaseAndLabelContainingIgnoreCaseOrRegionContainingIgnoreCaseAndCodeContainingIgnoreCase(
            @Param("region1") String region1, @Param("label") String label, @Param("region2") String region2, @Param("code") String code, Pageable pageable);
}
