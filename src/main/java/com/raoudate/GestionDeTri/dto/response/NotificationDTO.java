package com.raoudate.GestionDeTri.dto.response;

import com.raoudate.GestionDeTri.enums.NotificationEntity;
import com.raoudate.GestionDeTri.enums.NotificationStatus;
import com.raoudate.GestionDeTri.enums.NotificationType;
import com.raoudate.GestionDeTri.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Integer id;
    private NotificationType type;
    private NotificationEntity entityType;
    private Integer entityId;
    private String entityName;
    private String message;
    private NotificationStatus status;
    private Boolean actionRequired;
    private String reason;
    private String createdBy;
    private String initiatedBy;  
    private String targetUserId;
    private String details;
    private Instant createdAt;

    
    public static NotificationDTO fromEntity(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDTO dto = NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .entityName(notification.getEntityName())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .actionRequired(notification.getActionRequired())
                .reason(notification.getReason())
                .createdBy(notification.getCreatedBy())
                .initiatedBy(notification.getInitiatedBy())
                .targetUserId(notification.getTargetUserId())
                .details(notification.getDetails())
                .createdAt(notification.getDateCreation())
                .build();
        
        // Log pour debug
        if (notification.getStatus() == com.raoudate.GestionDeTri.enums.NotificationStatus.TRAITEE) {
            System.out.println(" Notification TRAITEE convertie - ID: " + notification.getId() + ", Reason: " + notification.getReason());
        }
        
        return dto;
    }

    /**
     * Convertit un DTO en entité Notification
     */
    public static Notification toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }

        return Notification.builder()
                .type(dto.getType())
                .entityType(dto.getEntityType())
                .entityId(dto.getEntityId())
                .entityName(dto.getEntityName())
                .message(dto.getMessage())
                .status(dto.getStatus())
                .actionRequired(dto.getActionRequired())
                .reason(dto.getReason())
                .createdBy(dto.getCreatedBy())
                .initiatedBy(dto.getInitiatedBy())
                .targetUserId(dto.getTargetUserId())
                .details(dto.getDetails())
                .build();
    }
}
