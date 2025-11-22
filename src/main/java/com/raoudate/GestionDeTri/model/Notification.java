package com.raoudate.GestionDeTri.model;

import com.raoudate.GestionDeTri.Enum.NotificationEntity;
import com.raoudate.GestionDeTri.Enum.NotificationStatus;
import com.raoudate.GestionDeTri.Enum.NotificationType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant une notification dans le système
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationEntity entityType;

    @Column(nullable = false)
    private Integer entityId;

    @Column(nullable = false)
    private String entityName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.NON_LUE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actionRequired = false;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = true)
    private String initiatedBy;  // Email du superviseur qui a initié la demande (pour les notifications de réponse)

    @Column(nullable = false)
    private String targetUserId;

    @Column(columnDefinition = "TEXT")
    private String details;
}
