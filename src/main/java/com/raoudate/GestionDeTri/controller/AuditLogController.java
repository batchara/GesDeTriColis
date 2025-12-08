package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.audit.AuditLog;
import com.raoudate.GestionDeTri.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostConstruct
    public void init() {
        log.info(" AuditLogController initialisé et prêt à recevoir des requêtes sur /api/v1/audit/**");
    }

    /**
     * Récupère tous les logs d'audit (Admin uniquement)
     */
    @GetMapping("/logs")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN_READ')")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        log.info(" Récupération de tous les logs d'audit");
        
        // Logger cette action
        auditLogService.logAction(
            "VIEW_AUDIT_LOGS",
            "AUDIT_LOG",
            null,
            null,
            null,
            "Consultation des logs d'audit"
        );
        
        List<AuditLog> logs = auditLogService.getAllLogs();
        log.info(" Nombre de logs récupérés: {}", logs.size());
        
        return ResponseEntity.ok(logs);
    }

    /**
     * Récupère les logs filtrés (Admin uniquement)
     */
    @GetMapping("/logs/filter")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN_READ')")
    public ResponseEntity<List<AuditLog>> getFilteredLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return ResponseEntity.ok(auditLogService.getFilteredLogs(action, username, entityType, startDate, endDate));
    }

    /**
     * Récupère les statistiques des logs (Admin uniquement)
     */
    @GetMapping("/logs/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN_READ')")
    public ResponseEntity<Map<String, Object>> getLogStatistics() {
        List<AuditLog> allLogs = auditLogService.getAllLogs();
        List<String> systemEntities = List.of("TOKEN", "AUTHENTICATION", "AUDIT", "SESSION");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allLogs.size());
        
        // Stats par action (toutes les actions)
        Map<String, Long> byAction = allLogs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getAction() != null ? log.getAction() : "UNKNOWN",
                Collectors.counting()
            ));
        stats.put("byAction", byAction);
        
        // Stats par action CRUD métier uniquement (sans les opérations système)
        Map<String, Long> byBusinessAction = allLogs.stream()
            .filter(log -> {
                String action = log.getAction();
                String entityType = log.getEntityType();
                boolean isCrudAction = "CREATE".equals(action) || "UPDATE".equals(action) || "DELETE".equals(action);
                boolean isNotSystemEntity = entityType == null || !systemEntities.contains(entityType.toUpperCase());
                return isCrudAction && isNotSystemEntity;
            })
            .collect(Collectors.groupingBy(
                log -> log.getAction() != null ? log.getAction() : "UNKNOWN",
                Collectors.counting()
            ));
        stats.put("byBusinessAction", byBusinessAction);
        
        // Stats par type d'entité (uniquement entités métier)
        Map<String, Long> byEntityType = allLogs.stream()
            .filter(log -> log.getEntityType() != null)
            .filter(log -> !systemEntities.contains(log.getEntityType().toUpperCase()))
            .collect(Collectors.groupingBy(
                AuditLog::getEntityType,
                Collectors.counting()
            ));
        stats.put("byEntityType", byEntityType);
        
        // Stats par utilisateur
        Map<String, Long> byUser = allLogs.stream()
            .filter(log -> log.getUsername() != null)
            .collect(Collectors.groupingBy(
                AuditLog::getUsername,
                Collectors.counting()
            ));
        stats.put("byUser", byUser);
        
        // Stats par statut
        Map<String, Long> byStatus = allLogs.stream()
            .filter(log -> log.getStatus() != null)
            .collect(Collectors.groupingBy(
                AuditLog::getStatus,
                Collectors.counting()
            ));
        stats.put("byStatus", byStatus);
        
        // Nombre de connexions/déconnexions
        long authenticationCount = allLogs.stream()
            .filter(log -> "AUTHENTICATION".equals(log.getAction()))
            .count();
        stats.put("authentications", authenticationCount);
        
        long successfulLogins = allLogs.stream()
            .filter(log -> "AUTHENTICATION".equals(log.getAction()) && "SUCCESS".equals(log.getStatus()))
            .count();
        stats.put("successfulLogins", successfulLogins);
        
        long failedLogins = allLogs.stream()
            .filter(log -> "AUTHENTICATION".equals(log.getAction()) && "FAILED".equals(log.getStatus()))
            .count();
        stats.put("failedLogins", failedLogins);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Récupère les logs d'authentification uniquement (connexions/déconnexions)
     */
    @GetMapping("/logs/authentication")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN_READ')")
    public ResponseEntity<List<AuditLog>> getAuthenticationLogs() {
        List<AuditLog> authLogs = auditLogService.getAllLogs().stream()
            .filter(log -> "AUTHENTICATION".equals(log.getAction()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(authLogs);
    }

    /**
     * Récupère les logs par utilisateur
     */
    @GetMapping("/logs/user/{username}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN_READ')")
    public ResponseEntity<List<AuditLog>> getLogsByUser(@PathVariable String username) {
        List<AuditLog> userLogs = auditLogService.getAllLogs().stream()
            .filter(log -> log.getUsername() != null && log.getUsername().equals(username))
            .collect(Collectors.toList());
        return ResponseEntity.ok(userLogs);
    }

    /**
     * Récupère les logs des CRUD uniquement (actions métier des utilisateurs)
     * Exclut les opérations système comme TOKEN, AUTHENTICATION, etc.
     */
    @GetMapping("/logs/crud")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN_READ')")
    public ResponseEntity<List<AuditLog>> getCrudLogs() {
        // Entités système à exclure
        List<String> systemEntities = List.of("TOKEN", "AUTHENTICATION", "AUDIT", "SESSION");
        
        List<AuditLog> crudLogs = auditLogService.getAllLogs().stream()
            .filter(log -> {
                String action = log.getAction();
                String entityType = log.getEntityType();
                
                // Filtrer par action CRUD
                boolean isCrudAction = "CREATE".equals(action) || 
                                      "UPDATE".equals(action) || 
                                      "DELETE".equals(action);
                
                // Exclure les entités système
                boolean isNotSystemEntity = entityType == null || 
                                           !systemEntities.contains(entityType.toUpperCase());
                
                return isCrudAction && isNotSystemEntity;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(crudLogs);
    }

    /**
     * Efface tous les logs (action sensible - Admin uniquement)
     */
    @DeleteMapping("/logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> clearLogs() {
        auditLogService.clearLogs();
        auditLogService.logAction(
            "CLEAR_LOGS", 
            "AUDIT", 
            null, 
            null, 
            null, 
            "Tous les logs d'audit ont été effacés"
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Tous les logs d'audit ont été effacés avec succès");
        return ResponseEntity.ok(response);
    }
}
