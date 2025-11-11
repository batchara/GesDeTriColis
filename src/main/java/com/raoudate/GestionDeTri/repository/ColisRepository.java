package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.Enum.StatutColis;
import com.raoudate.GestionDeTri.model.Agences;
import com.raoudate.GestionDeTri.model.Colis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ColisRepository extends JpaRepository<Colis, Integer> {

    Colis findByCodeSuivi(String codeSuivi);

    @Query("SELECT COUNT(c) FROM Colis c WHERE c.statut = :statut")
    Long countByStatut(@Param("statut") String statut);

    // Méthodes pour gérer les colis liés à une agence
    long countByAgenceAffectee(Agences agence);
    List<Colis> findByAgenceAffectee(Agences agence);

    // Méthodes pour le retour automatique après 30 jours
    List<Colis> findByStatutAndDateReceptionBefore(StatutColis statut, Instant dateLimit);
    List<Colis> findByStatutAndDateReceptionBetween(StatutColis statut, Instant dateDebut, Instant dateFin);

}
