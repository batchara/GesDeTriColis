package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.Operateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperateurRepository extends JpaRepository<Operateur, Integer> {
}
