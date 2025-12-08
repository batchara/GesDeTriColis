package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.dto.response.NotificationDTO;
import com.raoudate.GestionDeTri.enums.NotificationEntity;

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
    void approveDeleteRequest(Integer notificationId, NotificationEntity entityType, Integer entityId, String adminEmail);
    
    /**
     * Rejeter une demande de suppression
     */
    void rejectDeleteRequest(Integer notificationId, String reason, String adminEmail);
    
    /**
     * Approuver une demande de modification
     */
    void approveModificationRequest(Integer notificationId, NotificationEntity entityType, Integer entityId, String modificationsJson, String adminEmail);
    
    /**
     * Rejeter une demande de modification
     */
    void rejectModificationRequest(Integer notificationId, String reason, String adminEmail);
    
    /**
     * Supprimer une notification
     */
    void deleteNotification(Integer notificationId);
}
