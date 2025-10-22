package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.CentreDeTri;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CentreRepository extends JpaRepository<CentreDeTri, Integer> {
    CentreDeTri findByNom(String nom);

}
