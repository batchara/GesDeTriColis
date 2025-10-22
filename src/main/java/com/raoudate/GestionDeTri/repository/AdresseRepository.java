package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.Adresse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdresseRepository extends JpaRepository<Adresse, Integer> {

    Adresse findByRue(String rue);
}
