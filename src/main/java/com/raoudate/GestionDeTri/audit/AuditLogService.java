package com.raoudate.GestionDeTri.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuditLogService {

    private static final String LOG_FILE_PATH = "logs/audit.log";
    private static final int MAX_LOGS = 10000; // Limite de logs pour éviter les fichiers trop volumineux
    private final ObjectMapper objectMapper;

    public AuditLogService() {
        this.objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        this.objectMapper.registerModule(javaTimeModule);
        // Accepter les timestamps au format array ET string pour compatibilité
        this.objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, true);
    }

    @PostConstruct
    public void init() {
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            Path logFile = Paths.get(LOG_FILE_PATH);
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            }
        } catch (IOException e) {
            log.error("Erreur lors de l'initialisation du fichier de logs", e);
        }
    }

    /**
     * Enregistre un log d'audit
     */
    public void logAction(String action, String entityType, String entityId, 
                         String oldValue, String newValue, String message) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .userId(getCurrentUserId())
                    .username(getCurrentUsername())
                    .userRole(getCurrentUserRole())
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .ipAddress(getClientIpAddress())
                    .userAgent(getUserAgent())
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .status("SUCCESS")
                    .message(message)
                    .build();

            writeLogToFile(auditLog);
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement du log d'audit", e);
        }
    }

    /**
     * Log pour les authentifications
     */
    public void logAuthentication(String username, String status, String message, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .username(username)
                    .action("AUTHENTICATION")
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .status(status)
                    .message(message)
                    .build();

            writeLogToFile(auditLog);
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement du log d'authentification", e);
        }
    }

    /**
     * Log pour les erreurs
     */
    public void logError(String action, String entityType, String message, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .userId(getCurrentUserId())
                    .username(getCurrentUsername())
                    .userRole(getCurrentUserRole())
                    .action(action)
                    .entityType(entityType)
                    .ipAddress(getClientIpAddress())
                    .status("ERROR")
                    .message(message)
                    .details(details)
                    .build();

            writeLogToFile(auditLog);
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement du log d'erreur", e);
        }
    }

    /**
     * Récupère tous les logs
     */
    public List<AuditLog> getAllLogs() {
        try {
            Path logFile = Paths.get(LOG_FILE_PATH);
            if (!Files.exists(logFile)) {
                return Collections.emptyList();
            }

            List<String> lines = Files.readAllLines(logFile, StandardCharsets.UTF_8);
            List<AuditLog> logs = new ArrayList<>();
            
            for (String line : lines) {
                try {
                    if (!line.trim().isEmpty()) {
                        AuditLog log = objectMapper.readValue(line, AuditLog.class);
                        logs.add(log);
                    }
                } catch (Exception e) {
                    log.warn("Impossible de parser le log: {}", line);
                }
            }
            
            // Retourner les logs du plus récent au plus ancien
            Collections.reverse(logs);
            return logs;
        } catch (IOException e) {
            log.error("Erreur lors de la lecture des logs", e);
            return Collections.emptyList();
        }
    }

    /**
     * Récupère les logs filtrés
     */
    public List<AuditLog> getFilteredLogs(String action, String username, String entityType, String startDate, String endDate) {
        List<AuditLog> allLogs = getAllLogs();
        
        return allLogs.stream()
                .filter(l -> action == null || action.isEmpty() || l.getAction().equals(action))
                .filter(l -> username == null || username.isEmpty() || 
                        (l.getUsername() != null && l.getUsername().contains(username)))
                .filter(l -> entityType == null || entityType.isEmpty() ||
                        (l.getEntityType() != null && l.getEntityType().equals(entityType)))
                .collect(Collectors.toList());
    }

    /**
     * Efface tous les logs
     */
    public void clearLogs() {
        try {
            Path logFile = Paths.get(LOG_FILE_PATH);
            Files.writeString(logFile, "", StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Logs effacés par: {}", getCurrentUsername());
        } catch (IOException e) {
            log.error("Erreur lors de l'effacement des logs", e);
        }
    }

    // ========== Méthodes utilitaires ==========

    private void writeLogToFile(AuditLog auditLog) throws IOException {
        String jsonLog = objectMapper.writeValueAsString(auditLog);
        Path logFile = Paths.get(LOG_FILE_PATH);
        
        // Vérifier la taille et faire une rotation si nécessaire
        List<String> existingLogs = Files.readAllLines(logFile);
        if (existingLogs.size() >= MAX_LOGS) {
            // Garder seulement les 80% plus récents
            int keepCount = (int) (MAX_LOGS * 0.8);
            List<String> logsToKeep = existingLogs.subList(
                Math.max(0, existingLogs.size() - keepCount), 
                existingLogs.size()
            );
            Files.write(logFile, logsToKeep, StandardCharsets.UTF_8, 
                       StandardOpenOption.TRUNCATE_EXISTING);
        }
        
        Files.writeString(logFile, jsonLog + System.lineSeparator(), 
                         StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            // Extraire uniquement le rôle principal (ROLE_ADMIN, ROLE_SUPERVISEUR, ROLE_OPERATEUR)
            // et masquer les permissions détaillées pour des raisons de sécurité
            return auth.getAuthorities().stream()
                    .map(Object::toString)
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .findFirst()
                    .orElse("UNKNOWN");
        }
        return "UNKNOWN";
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) 
                RequestContextHolder.getRequestAttributes();
            
            if (attributes == null) {
                return "UNKNOWN";
            }
            
            HttpServletRequest request = attributes.getRequest();
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String remoteAddr = request.getRemoteAddr();
            return remoteAddr != null ? remoteAddr : "UNKNOWN";
        } catch (Exception e) {
            log.debug("Impossible de récupérer l'adresse IP: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) 
                RequestContextHolder.getRequestAttributes();
            
            if (attributes == null) {
                return "UNKNOWN";
            }
            
            HttpServletRequest request = attributes.getRequest();
            String userAgent = request.getHeader("User-Agent");
            return userAgent != null ? userAgent : "UNKNOWN";
        } catch (Exception e) {
            log.debug("Impossible de récupérer le User-Agent: {}", e.getMessage());
            return "UNKNOWN";
        }
    }
}
