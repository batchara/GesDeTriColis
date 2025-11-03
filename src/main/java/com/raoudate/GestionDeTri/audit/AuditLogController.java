package com.raoudate.GestionDeTri.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Récupère tous les logs d'audit
     */
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    /**
     * Récupère les logs filtrés
     */
    @GetMapping("/logs/filter")
    public ResponseEntity<List<AuditLog>> getFilteredLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return ResponseEntity.ok(auditLogService.getFilteredLogs(action, username, startDate, endDate));
    }

    /**
     * Efface tous les logs (action sensible)
     */
    @DeleteMapping("/logs")
    public ResponseEntity<Void> clearLogs() {
        auditLogService.clearLogs();
        auditLogService.logAction(
            "CLEAR_LOGS", 
            "AUDIT", 
            null, 
            null, 
            null, 
            "Tous les logs d'audit ont été effacés"
        );
        return ResponseEntity.ok().build();
    }
}
