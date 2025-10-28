package com.raoudate.GestionDeTri.services.api;

import com.raoudate.GestionDeTri.Dto.NotificationDTO;
import com.raoudate.GestionDeTri.Enum.NotificationEntity;

import java.util.List;

public interface NotificationService {
    
    /**
     * Créer une nouvelle notification
     */
    NotificationDTO createNotification(NotificationDTO notificationDTO);
    
    /**
     * Récupérer toutes les notifications d'un utilisateur
     */
    List<NotificationDTO> getAllNotifications(String userEmail);
    
    /**
     * Récupérer les notifications non lues d'un utilisateur
     */
    List<NotificationDTO> getUnreadNotifications(String userEmail);
    
    /**
     * Compter les notifications non lues
     */
    long getUnreadCount(String userEmail);
    
    /**
     * Marquer une notification comme lue
     */
    NotificationDTO markAsRead(Integer notificationId);
    
    /**
     * Marquer toutes les notifications comme lues
     */
    void markAllAsRead(String userEmail);
    
    /**
     * Approuver une demande de suppression
     */
    void approveDeleteRequest(Integer notificationId, NotificationEntity entityType, Integer entityId);
    
    /**
     * Rejeter une demande de suppression
     */
    void rejectDeleteRequest(Integer notificationId, String reason);
    
    /**
     * Approuver une demande de modification
     */
    void approveModificationRequest(Integer notificationId, NotificationEntity entityType, Integer entityId, String modificationsJson);
    
    /**
     * Rejeter une demande de modification
     */
    void rejectModificationRequest(Integer notificationId, String reason);
    
    /**
     * Supprimer une notification
     */
    void deleteNotification(Integer notificationId);
}
