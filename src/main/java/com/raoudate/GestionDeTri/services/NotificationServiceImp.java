package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.Dto.NotificationDTO;
import com.raoudate.GestionDeTri.Enum.NotificationEntity;
import com.raoudate.GestionDeTri.Enum.NotificationStatus;
import com.raoudate.GestionDeTri.model.Agences;
import com.raoudate.GestionDeTri.model.Colis;
import com.raoudate.GestionDeTri.model.Notification;
import com.raoudate.GestionDeTri.model.User;
import com.raoudate.GestionDeTri.repository.AgenceRepository;
import com.raoudate.GestionDeTri.repository.ColisRepository;
import com.raoudate.GestionDeTri.repository.NotificationRepository;
import com.raoudate.GestionDeTri.repository.TokenRepository;
import com.raoudate.GestionDeTri.repository.UserRepository;
import com.raoudate.GestionDeTri.services.api.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImp implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AgenceRepository agenceRepository;
    private final ColisRepository colisRepository;
    private final TokenRepository tokenRepository;

    @Override
    @Transactional
    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        log.info("Création d'une notification pour l'utilisateur: {}", notificationDTO.getTargetUserId());
        
        // Si targetUserId est null et que c'est une notification avec action requise,
        // on assigne automatiquement à un administrateur
        if (notificationDTO.getTargetUserId() == null && Boolean.TRUE.equals(notificationDTO.getActionRequired())) {
            User admin = userRepository.findFirstByRolesName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Aucun administrateur trouvé dans le système"));
            notificationDTO.setTargetUserId(admin.getEmail());
            log.info("Notification assignée automatiquement à l'administrateur: {}", admin.getEmail());
        }
        
        Notification notification = NotificationDTO.toEntity(notificationDTO);
        Notification savedNotification = notificationRepository.save(notification);
        
        log.info("Notification créée avec succès: ID {}", savedNotification.getId());
        return NotificationDTO.fromEntity(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications(String userEmail) {
        log.info("Récupération de toutes les notifications pour: {}", userEmail);
        
        List<Notification> notifications = notificationRepository
                .findByTargetUserIdOrderByDateCreationDesc(userEmail);
        
        long traiteeCount = notifications.stream()
                .filter(n -> n.getStatus() == NotificationStatus.TRAITEE)
                .count();
        
        log.info("📊 Total notifications: {}, dont TRAITEE: {}", notifications.size(), traiteeCount);
        
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(String userEmail) {
        log.info("Récupération des notifications non lues pour: {}", userEmail);
        
        List<Notification> notifications = notificationRepository
                .findByTargetUserIdAndStatusOrderByDateCreationDesc(userEmail, NotificationStatus.NON_LUE);
        
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String userEmail) {
        return notificationRepository.countByTargetUserIdAndStatus(userEmail, NotificationStatus.NON_LUE);
    }

    @Override
    @Transactional
    public NotificationDTO markAsRead(Integer notificationId) {
        log.info("Marquage de la notification {} comme lue", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("Notification introuvable: " + notificationId));
        
        notification.setStatus(NotificationStatus.LUE);
        Notification updatedNotification = notificationRepository.save(notification);
        
        return NotificationDTO.fromEntity(updatedNotification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userEmail) {
        log.info("Marquage de toutes les notifications comme lues pour: {}", userEmail);
        
        List<Notification> notifications = notificationRepository
                .findByTargetUserIdAndStatusOrderByDateCreationDesc(userEmail, NotificationStatus.NON_LUE);
        
        notifications.forEach(notification -> notification.setStatus(NotificationStatus.LUE));
        notificationRepository.saveAll(notifications);
        
        log.info("{} notifications marquées comme lues", notifications.size());
    }

    @Override
    @Transactional
    public void approveDeleteRequest(Integer notificationId, NotificationEntity entityType, Integer entityId) {
        log.info("Approbation de la suppression: notification {}, entité {} ID {}", 
                notificationId, entityType, entityId);
        
        // Récupérer la notification originale
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("Notification introuvable: " + notificationId));
        
        // Vérifier que c'est une demande de suppression
        if (!notification.getActionRequired()) {
            throw new IllegalStateException("Cette notification ne nécessite pas d'action");
        }
        
        // Sauvegarder les infos pour la notification de retour
        String createdBy = notification.getCreatedBy();
        String entityName = notification.getEntityName();
        
        // Effectuer la suppression selon le type d'entité
        switch (entityType) {
            case UTILISATEUR:
                // ✅ SOFT DELETE: Désactiver l'utilisateur au lieu de le supprimer
                if (userRepository.existsById(entityId)) {
                    User userToDelete = userRepository.findById(entityId).get();
                    userToDelete.setDeleted(true);
                    userToDelete.setDeletedAt(java.time.Instant.now());
                    userToDelete.setDeletedBy(createdBy);
                    userToDelete.setEnabled(false); // Désactiver le compte aussi
                    userRepository.save(userToDelete);
                    log.info("✅ Utilisateur {} désactivé (soft delete)", entityId);
                } else {
                    log.warn("⚠️ Utilisateur {} déjà supprimé, notification marquée comme traitée", entityId);
                }
                break;
                
            case AGENCE:
                // ✅ SOFT DELETE: Désactiver l'agence au lieu de la supprimer
                if (agenceRepository.existsById(entityId)) {
                    Agences agenceToDelete = agenceRepository.findById(entityId).get();
                    agenceToDelete.setDeleted(true);
                    agenceToDelete.setDeletedAt(java.time.Instant.now());
                    agenceToDelete.setDeletedBy(createdBy);
                    agenceToDelete.setStatus("DELETED"); // Marquer comme supprimée
                    agenceRepository.save(agenceToDelete);
                    log.info("✅ Agence {} désactivée (soft delete)", entityId);
                } else {
                    log.warn("⚠️ Agence {} déjà supprimée, notification marquée comme traitée", entityId);
                }
                break;
                
            case COLIS:
                // ✅ SOFT DELETE: Désactiver le colis au lieu de le supprimer
                if (colisRepository.existsById(entityId)) {
                    Colis colisToDelete = colisRepository.findById(entityId).get();
                    colisToDelete.setDeleted(true);
                    colisToDelete.setDeletedAt(java.time.Instant.now());
                    colisToDelete.setDeletedBy(createdBy);
                    colisRepository.save(colisToDelete);
                    log.info("✅ Colis {} désactivé (soft delete)", entityId);
                } else {
                    log.warn("⚠️ Colis {} déjà supprimé, notification marquée comme traitée", entityId);
                }
                break;
            default:
                throw new IllegalStateException("Type d'entité non géré: " + entityType);
        }
        
        // ✅ Mettre à jour la notification originale et la garder dans l'historique
        notification.setStatus(NotificationStatus.TRAITEE);
        notification.setActionRequired(false); // Plus d'action requise
        notification.setReason("Approuvée par l'administrateur");
        Notification savedNotification = notificationRepository.save(notification);
        log.info("📚 Notification {} marquée comme TRAITEE et conservée dans l'historique", notificationId);
        log.info("📚 Vérification après save - Status: {}, ActionRequired: {}, Reason: {}", 
                savedNotification.getStatus(), savedNotification.getActionRequired(), savedNotification.getReason());
        
        // Créer une notification de réponse pour le superviseur
        Notification responseNotification = Notification.builder()
                .type(notification.getType()) // Même type (SUPPRESSION_DEMANDE)
                .message(String.format("✅ Votre demande de suppression de %s '%s' a été APPROUVÉE par l'administrateur. La suppression a été effectuée avec succès.", 
                        entityType.name().toLowerCase(), entityName))
                .targetUserId(createdBy) // Envoyer au superviseur qui a créé la demande
                .createdBy("ADMIN") // Créée par l'admin
                .status(NotificationStatus.NON_LUE)
                .actionRequired(false) // Pas d'action requise, juste une info
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .build();
        
        notificationRepository.save(responseNotification);
        log.info("Notification de réponse (approbation) envoyée au superviseur: {}", createdBy);
        
        log.info("Suppression approuvée et effectuée avec succès");
    }

    @Override
    @Transactional
    public void rejectDeleteRequest(Integer notificationId, String reason) {
        log.info("Rejet de la suppression: notification {}, raison: {}", notificationId, reason);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("Notification introuvable: " + notificationId));
        
        if (!notification.getActionRequired()) {
            throw new IllegalStateException("Cette notification ne nécessite pas d'action");
        }
        
        // Sauvegarder les infos pour la notification de retour
        String createdBy = notification.getCreatedBy();
        String entityName = notification.getEntityName();
        NotificationEntity entityType = notification.getEntityType();
        Integer entityId = notification.getEntityId();
        
        // ✅ Mettre à jour la notification originale et la garder dans l'historique
        notification.setStatus(NotificationStatus.TRAITEE);
        notification.setActionRequired(false); // Plus d'action requise
        notification.setReason(reason != null ? reason : "Rejetée par l'administrateur");
        notificationRepository.save(notification);
        log.info("Notification {} marquée comme TRAITEE et conservée dans l'historique", notificationId);
        
        // Créer une notification de réponse pour le superviseur
        Notification responseNotification = Notification.builder()
                .type(notification.getType()) // Même type
                .message(String.format("❌ Votre demande de suppression de %s '%s' a été REJETÉE par l'administrateur. Raison: %s", 
                        entityType.name().toLowerCase(), entityName, 
                        reason != null ? reason : "Non spécifiée"))
                .targetUserId(createdBy) // Envoyer au superviseur qui a créé la demande
                .createdBy("ADMIN") // Créée par l'admin
                .status(NotificationStatus.NON_LUE)
                .actionRequired(false) // Pas d'action requise, juste une info
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .build();
        
        notificationRepository.save(responseNotification);
        log.info("Notification de réponse (rejet) envoyée au superviseur: {}", createdBy);
        
        log.info("Demande de suppression rejetée");
    }

    @Override
    @Transactional
    public void deleteNotification(Integer notificationId) {
        log.info("Suppression de la notification: {}", notificationId);
        
        if (!notificationRepository.existsById(notificationId)) {
            throw new IllegalStateException("Notification introuvable: " + notificationId);
        }
        
        notificationRepository.deleteById(notificationId);
        log.info("Notification supprimée avec succès");
    }

    @Override
    @Transactional
    public void approveModificationRequest(Integer notificationId, NotificationEntity entityType, Integer entityId, String modificationsJson) {
        log.info("Approbation de la modification: notification {}, entité {} ID {}", 
                notificationId, entityType, entityId);
        
        // Récupérer la notification originale
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("Notification introuvable: " + notificationId));
        
        // Vérifier que c'est une demande de modification
        if (!notification.getActionRequired()) {
            throw new IllegalStateException("Cette notification ne nécessite pas d'action");
        }
        
        // Sauvegarder les infos pour la notification de retour
        String createdBy = notification.getCreatedBy();
        String entityName = notification.getEntityName();
        
        // Note: La modification réelle de l'agence sera faite dans le contrôleur
        // car nous avons besoin des données complètes de l'agence
        
        // ✅ Mettre à jour la notification originale et la garder dans l'historique
        notification.setStatus(NotificationStatus.TRAITEE);
        notification.setActionRequired(false); // Plus d'action requise
        notification.setReason("Approuvée par l'administrateur");
        notificationRepository.save(notification);
        log.info("Notification {} marquée comme TRAITEE et conservée dans l'historique", notificationId);
        
        // Créer une notification de réponse pour le superviseur
        Notification responseNotification = Notification.builder()
                .type(notification.getType())
                .message(String.format("✅ Votre demande de modification de %s '%s' a été APPROUVÉE par l'administrateur.", 
                        entityType.name().toLowerCase(), entityName))
                .targetUserId(createdBy)
                .createdBy("ADMIN")
                .status(NotificationStatus.NON_LUE)
                .actionRequired(false)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .build();
        
        notificationRepository.save(responseNotification);
        log.info("Notification de réponse (approbation modification) envoyée au superviseur: {}", createdBy);
    }

    @Override
    @Transactional
    public void rejectModificationRequest(Integer notificationId, String reason) {
        log.info("Rejet de la modification: notification {}, raison: {}", notificationId, reason);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("Notification introuvable: " + notificationId));
        
        if (!notification.getActionRequired()) {
            throw new IllegalStateException("Cette notification ne nécessite pas d'action");
        }
        
        // Sauvegarder les infos pour la notification de retour
        String createdBy = notification.getCreatedBy();
        String entityName = notification.getEntityName();
        NotificationEntity entityType = notification.getEntityType();
        Integer entityId = notification.getEntityId();
        
        // ✅ Mettre à jour la notification originale et la garder dans l'historique
        notification.setStatus(NotificationStatus.TRAITEE);
        notification.setActionRequired(false); // Plus d'action requise
        notification.setReason(reason != null ? reason : "Rejetée par l'administrateur");
        notificationRepository.save(notification);
        log.info("Notification {} marquée comme TRAITEE et conservée dans l'historique", notificationId);
        
        // Créer une notification de réponse pour le superviseur
        Notification responseNotification = Notification.builder()
                .type(notification.getType())
                .message(String.format("❌ Votre demande de modification de %s '%s' a été REJETÉE par l'administrateur. Raison: %s", 
                        entityType.name().toLowerCase(), entityName, 
                        reason != null ? reason : "Non spécifiée"))
                .targetUserId(createdBy)
                .createdBy("ADMIN")
                .status(NotificationStatus.NON_LUE)
                .actionRequired(false)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .build();
        
        notificationRepository.save(responseNotification);
        log.info("Notification de réponse (rejet modification) envoyée au superviseur: {}", createdBy);
    }
    
}
