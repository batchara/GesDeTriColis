package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.Dto.NotificationDTO;
import com.raoudate.GestionDeTri.Enum.NotificationEntity;
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
@CrossOrigin(origins = "http://localhost:4200")
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
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
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
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
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
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        String userEmail = authentication.getName();
        long count = notificationService.getUnreadCount(userEmail);
        return ResponseEntity.ok(count);
    }

    /**
     * Marquer une notification comme lue
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Integer id) {
        log.info("Marquage de la notification {} comme lue", id);
        
        NotificationDTO notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
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
            @RequestBody Map<String, Object> payload) {
        
        NotificationEntity entityType = NotificationEntity.valueOf((String) payload.get("entityType"));
        Integer entityId = (Integer) payload.get("entityId");
        
        log.info("Approbation de la suppression: notification={}, entité={}, entityId={}", 
                id, entityType, entityId);
        
        notificationService.approveDeleteRequest(id, entityType, entityId);
        return ResponseEntity.ok().build();
    }

    /**
     * Rejeter une demande de suppression
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> rejectDeleteRequest(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        
        String reason = payload.get("reason");
        log.info("Rejet de la suppression: notification={}, raison={}", id, reason);
        
        notificationService.rejectDeleteRequest(id, reason);
        return ResponseEntity.ok().build();
    }

    /**
     * Approuver une demande de modification
     */
    @PostMapping("/{id}/approve-modification")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> approveModificationRequest(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload) {
        
        NotificationEntity entityType = NotificationEntity.valueOf((String) payload.get("entityType"));
        Integer entityId = (Integer) payload.get("entityId");
        String modifications = (String) payload.get("modifications");
        
        log.info("Approbation de la modification: notification={}, entité={}, entityId={}", 
                id, entityType, entityId);
        
        notificationService.approveModificationRequest(id, entityType, entityId, modifications);
        return ResponseEntity.ok().build();
    }

    /**
     * Rejeter une demande de modification
     */
    @PostMapping("/{id}/reject-modification")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> rejectModificationRequest(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        
        String reason = payload.get("reason");
        log.info("Rejet de la modification: notification={}, raison={}", id, reason);
        
        notificationService.rejectModificationRequest(id, reason);
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
