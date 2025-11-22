package com.raoudate.GestionDeTri.audit;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogInitializer {

    private final AuditLogService auditLogService;
    private static final String LOG_FILE_PATH = "logs/audit.log";

    @EventListener(ApplicationReadyEvent.class)
    public void initializeAuditLogs() {
        try {
            Path logFile = Paths.get(LOG_FILE_PATH);
            
            // Vérifier si le fichier existe et est vide
            if (!Files.exists(logFile) || Files.size(logFile) == 0) {
                log.info("📝 Initialisation des logs d'audit avec des entrées de test...");
                
                // Créer quelques logs de test
                auditLogService.logAuthentication(
                    "admin@example.com",
                    "SUCCESS",
                    "Connexion réussie de l'administrateur",
                    "127.0.0.1",
                    "Mozilla/5.0"
                );
                
                auditLogService.logAction(
                    "CREATE",
                    "USER",
                    "1",
                    null,
                    "Utilisateur créé",
                    "Création d'un nouvel utilisateur"
                );
                
                auditLogService.logAction(
                    "UPDATE",
                    "COLIS",
                    "123",
                    "STATUT: EN_ATTENTE",
                    "STATUT: EN_COURS",
                    "Mise à jour du statut du colis"
                );
                
                auditLogService.logAction(
                    "DELETE",
                    "AGENCE",
                    "5",
                    "Agence Test",
                    null,
                    "Suppression de l'agence de test"
                );
                
                auditLogService.logError(
                    "LOGIN_FAILED",
                    "AUTHENTICATION",
                    "Tentative de connexion échouée",
                    "Mot de passe incorrect pour user@example.com"
                );
                
                log.info("✅ Logs d'audit initialisés avec succès");
            } else {
                long logCount = Files.lines(logFile).count();
                log.info("📊 Fichier audit.log existant avec {} entrées", logCount);
            }
        } catch (IOException e) {
            log.error("❌ Erreur lors de l'initialisation des logs d'audit", e);
        }
    }
}
