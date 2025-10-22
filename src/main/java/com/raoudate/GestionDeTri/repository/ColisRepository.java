package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.Colis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColisRepository extends JpaRepository<Colis, Integer> {

    Colis findByCodeSuivi(String codeSuivi);

}
