package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.Superviseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuperviseurRepository extends JpaRepository<Superviseur, Integer> {
}
