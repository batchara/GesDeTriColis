package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.Agences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgenceRepository extends JpaRepository<Agences, Integer> {

    Optional<Agences> findByLabel(String label);

    Optional<Agences> findByCode(String code);
}
