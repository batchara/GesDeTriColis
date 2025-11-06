package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.Agences;
import com.raoudate.GestionDeTri.model.Colis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ColisRepository extends JpaRepository<Colis, Integer> {

    Colis findByCodeSuivi(String codeSuivi);

    @Query("SELECT COUNT(c) FROM Colis c WHERE c.statut = :statut")
    Long countByStatut(@Param("statut") String statut);

    // Méthodes pour gérer les colis liés à une agence
    long countByAgenceAffectee(Agences agence);
    List<Colis> findByAgenceAffectee(Agences agence);

}
