package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.dto.response.NotificationDTO;
import com.raoudate.GestionDeTri.enums.NotificationEntity;
import com.raoudate.GestionDeTri.services.NotificationServiceImp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
// Pas besoin de @CrossOrigin ici car déjà configuré globalement dans SecurityConfig
public class NotificationController {

    private final NotificationServiceImp notificationService;

    /**
     * Créer une nouvelle notification
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<NotificationDTO> createNotification(@RequestBody NotificationDTO notificationDTO) {
        log.info("Création d'une notification: type={}, entité={}", 
                notificationDTO.getType(), notificationDTO.getEntityType());
        
        NotificationDTO created = notificationService.createNotification(notificationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Récupérer toutes les notifications de l'utilisateur connecté
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR', 'ROLE_OPERATEUR')")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Récupération des notifications pour: {}", userEmail);
        
        List<NotificationDTO> notifications = notificationService.getAllNotifications(userEmail);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Récupérer les notifications non lues
     */
    @GetMapping("/unread")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR', 'ROLE_OPERATEUR')")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Récupération des notifications non lues pour: {}", userEmail);
        
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userEmail);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Compter les notifications non lues
     */
    @GetMapping("/unread/count")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR', 'ROLE_OPERATEUR')")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        String userEmail = authentication.getName();
        long count = notificationService.getUnreadCount(userEmail);
        return ResponseEntity.ok(count);
    }

    /**
     * Marquer une notification comme lue
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR', 'ROLE_OPERATEUR')")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Integer id) {
        log.info("Marquage de la notification {} comme lue", id);
        
        NotificationDTO notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR', 'ROLE_OPERATEUR')")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Marquage de toutes les notifications comme lues pour: {}", userEmail);
        
        notificationService.markAllAsRead(userEmail);
        return ResponseEntity.ok().build();
    }

    /**
     * Approuver une demande de suppression
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> approveDeleteRequest(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {
        
        NotificationEntity entityType = NotificationEntity.valueOf((String) payload.get("entityType"));
        Integer entityId = (Integer) payload.get("entityId");
        String adminEmail = authentication.getName();
        
        log.info("Approbation de la suppression: notification={}, entité={}, entityId={}, admin={}", 
                id, entityType, entityId, adminEmail);
        
        notificationService.approveDeleteRequest(id, entityType, entityId, adminEmail);
        return ResponseEntity.ok().build();
    }

    /**
     * Rejeter une demande de suppression
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> rejectDeleteRequest(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        
        String reason = payload.get("reason");
        String adminEmail = authentication.getName();
        log.info("Rejet de la suppression: notification={}, raison={}, admin={}", id, reason, adminEmail);
        
        notificationService.rejectDeleteRequest(id, reason, adminEmail);
        return ResponseEntity.ok().build();
    }

    /**
     * Approuver une demande de modification
     */
    @PostMapping("/{id}/approve-modification")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> approveModificationRequest(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {
        
        NotificationEntity entityType = NotificationEntity.valueOf((String) payload.get("entityType"));
        Integer entityId = (Integer) payload.get("entityId");
        String modifications = (String) payload.get("modifications");
        String adminEmail = authentication.getName();
        
        log.info("Approbation de la modification: notification={}, entité={}, entityId={}, admin={}", 
                id, entityType, entityId, adminEmail);
        
        notificationService.approveModificationRequest(id, entityType, entityId, modifications, adminEmail);
        return ResponseEntity.ok().build();
    }

    /**
     * Rejeter une demande de modification
     */
    @PostMapping("/{id}/reject-modification")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> rejectModificationRequest(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        
        String reason = payload.get("reason");
        String adminEmail = authentication.getName();
        log.info("Rejet de la modification: notification={}, raison={}, admin={}", id, reason, adminEmail);
        
        notificationService.rejectModificationRequest(id, reason, adminEmail);
        return ResponseEntity.ok().build();
    }

    /**
     * Supprimer une notification
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer id) {
        log.info("Suppression de la notification: {}", id);
        
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
