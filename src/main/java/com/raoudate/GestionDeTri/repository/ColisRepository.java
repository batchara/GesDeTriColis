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

    // ✅ Récupérer TOUS les colis NON supprimés (méthode principale)
    @Query("SELECT c FROM Colis c WHERE c.isDeleted = false ORDER BY c.dateEnvoi DESC")
    List<Colis> findAllActive();
    
    // ✅ Recherche sans inclure les colis supprimés
    @Query("SELECT c FROM Colis c WHERE c.codeSuivi = :codeSuivi AND c.isDeleted = false")
    Colis findByCodeSuivi(@Param("codeSuivi") String codeSuivi);
    
    // ✅ Vérifier si un colis avec ce code de suivi existe (ACTIF uniquement, pas supprimé)
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Colis c WHERE c.codeSuivi = :codeSuivi AND c.isDeleted = false")
    boolean existsByCodeSuivi(@Param("codeSuivi") String codeSuivi);

    // ✅ Compter uniquement les colis NON supprimés
    @Query("SELECT COUNT(c) FROM Colis c WHERE c.statut = :statut AND c.isDeleted = false")
    Long countByStatut(@Param("statut") String statut);

    // ✅ Compter les colis par statut (enum) - NON supprimés
    @Query("SELECT COUNT(c) FROM Colis c WHERE c.statut = :statut AND c.isDeleted = false")
    Long countByStatutEnum(@Param("statut") StatutColis statut);

    // ✅ Méthodes pour gérer les colis liés à une agence - NON supprimés
    @Query("SELECT COUNT(c) FROM Colis c WHERE c.agenceAffectee = :agence AND c.isDeleted = false")
    long countByAgenceAffectee(@Param("agence") Agences agence);
    
    @Query("SELECT c FROM Colis c WHERE c.agenceAffectee = :agence AND c.isDeleted = false")
    List<Colis> findByAgenceAffectee(@Param("agence") Agences agence);
    
    // ✅ Compter par agence et statut - NON supprimés
    @Query("SELECT COUNT(c) FROM Colis c WHERE c.agenceAffectee = :agence AND c.statut = :statut AND c.isDeleted = false")
    long countByAgenceAffecteeAndStatut(@Param("agence") Agences agence, @Param("statut") StatutColis statut);

    // ✅ Méthodes pour le retour automatique après 30 jours - NON supprimés
    @Query("SELECT c FROM Colis c WHERE c.statut = :statut AND c.dateReception < :dateLimit AND c.isDeleted = false")
    List<Colis> findByStatutAndDateReceptionBefore(@Param("statut") StatutColis statut, @Param("dateLimit") Instant dateLimit);
    
    @Query("SELECT c FROM Colis c WHERE c.statut = :statut AND c.dateReception BETWEEN :dateDebut AND :dateFin AND c.isDeleted = false")
    List<Colis> findByStatutAndDateReceptionBetween(@Param("statut") StatutColis statut, @Param("dateDebut") Instant dateDebut, @Param("dateFin") Instant dateFin);
    
    // ✅ Compter par date d'envoi - NON supprimés
    @Query("SELECT COUNT(c) FROM Colis c WHERE c.dateEnvoi BETWEEN :start AND :end AND c.isDeleted = false")
    long countByDateEnvoiBetween(@Param("start") Instant start, @Param("end") Instant end);
    
    // ✅ Compter par statut et date - NON supprimés
    @Query("SELECT COUNT(c) FROM Colis c WHERE c.statut = :statut AND c.dateEnvoi BETWEEN :start AND :end AND c.isDeleted = false")
    long countByStatutAndDateEnvoiBetween(@Param("statut") StatutColis statut, @Param("start") Instant start, @Param("end") Instant end);
    
    // ✅ Compter par statut (not in) - NON supprimés
    @Query("SELECT COUNT(c) FROM Colis c WHERE c.statut NOT IN :statuts AND c.isDeleted = false")
    long countByStatutNotIn(@Param("statuts") List<StatutColis> statuts);
    
    // ✅ Top agences par nombre de colis - NON supprimés
    @Query("SELECT c.agenceAffectee, COUNT(c) as nbColis FROM Colis c WHERE c.agenceAffectee IS NOT NULL AND c.isDeleted = false GROUP BY c.agenceAffectee ORDER BY nbColis DESC")
    List<Object[]> findTopAgencesByColisCount();
    
    // ✅ Statistiques par région - NON supprimés
    @Query("SELECT a.region, COUNT(c) FROM Colis c JOIN c.agenceAffectee a WHERE a.region IS NOT NULL AND c.isDeleted = false GROUP BY a.region")
    List<Object[]> findColisCountByRegion();

}
