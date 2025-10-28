package com.raoudate.GestionDeTri.Dto;

import com.raoudate.GestionDeTri.Enum.NotificationEntity;
import com.raoudate.GestionDeTri.Enum.NotificationStatus;
import com.raoudate.GestionDeTri.Enum.NotificationType;
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
    private String targetUserId;
    private String details;
    private Instant createdAt;

    /**
     * Convertit une entité Notification en DTO
     */
    public static NotificationDTO fromEntity(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationDTO.builder()
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
                .targetUserId(notification.getTargetUserId())
                .details(notification.getDetails())
                .createdAt(notification.getDateCreation())
                .build();
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
                .targetUserId(dto.getTargetUserId())
                .details(dto.getDetails())
                .build();
    }
}
