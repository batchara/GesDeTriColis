package com.raoudate.GestionDeTri.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    private LocalDateTime timestamp;
    private String userId;
    private String username;
    private String userRole;
    private String action;
    private String entityType;
    private String entityId;
    private String ipAddress;
    private String userAgent;
    private String oldValue;
    private String newValue;
    private String status;
    private String message;
    private String details;
}
