package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.Enum.NotificationStatus;
import com.raoudate.GestionDeTri.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /**
     * Récupère toutes les notifications d'un utilisateur, triées par date de création décroissante
     */
    List<Notification> findByTargetUserIdOrderByDateCreationDesc(String targetUserId);

    /**
     * Récupère les notifications d'un utilisateur avec un statut spécifique
     */
    List<Notification> findByTargetUserIdAndStatusOrderByDateCreationDesc(String targetUserId, NotificationStatus status);

    /**
     * Compte le nombre de notifications non lues pour un utilisateur
     */
    long countByTargetUserIdAndStatus(String targetUserId, NotificationStatus status);

    /**
     * Récupère les notifications nécessitant une action (demandes de suppression en attente)
     */
    List<Notification> findByTargetUserIdAndActionRequiredTrueAndStatusOrderByDateCreationDesc(
            String targetUserId, NotificationStatus status);
}
