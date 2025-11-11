package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.Agences;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgenceRepository extends JpaRepository<Agences, Integer> {

    Optional<Agences> findByLabel(String label);

    Optional<Agences> findByCode(String code);
    
    // Méthodes de pagination et recherche
    Page<Agences> findByRegionContainingIgnoreCase(String region, Pageable pageable);
    
    Page<Agences> findByLabelContainingIgnoreCase(String label, Pageable pageable);
    
    Page<Agences> findByCodeContainingIgnoreCase(String code, Pageable pageable);
    
    Page<Agences> findByLabelContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String label, String code, Pageable pageable);
    
    // Recherche combinée : région + (nom OU code)
    Page<Agences> findByRegionContainingIgnoreCaseAndLabelContainingIgnoreCase(
            String region, String label, Pageable pageable);
    
    Page<Agences> findByRegionContainingIgnoreCaseAndCodeContainingIgnoreCase(
            String region, String code, Pageable pageable);
    
    Page<Agences> findByRegionContainingIgnoreCaseAndLabelContainingIgnoreCaseOrRegionContainingIgnoreCaseAndCodeContainingIgnoreCase(
            String region1, String label, String region2, String code, Pageable pageable);
}
