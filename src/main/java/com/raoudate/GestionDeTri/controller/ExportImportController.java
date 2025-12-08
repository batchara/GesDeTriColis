package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.services.ExportImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/export-import")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Export/Import", description = "Endpoints pour l'export et l'import de données en Excel et CSV")
public class ExportImportController {

    private final ExportImportService exportImportService;

    // ==================== EXPORT UTILISATEURS ====================

    @GetMapping("/users/export/excel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    @Operation(summary = "Exporter les utilisateurs en Excel", description = "Télécharge un fichier Excel contenant tous les utilisateurs")
    public ResponseEntity<InputStreamResource> exportUsersToExcel() {
        log.info(" Export des utilisateurs en Excel");
        
        ByteArrayInputStream in = exportImportService.exportUsersToExcel();
        String filename = "utilisateurs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/users/export/csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    @Operation(summary = "Exporter les utilisateurs en CSV", description = "Télécharge un fichier CSV contenant tous les utilisateurs")
    public ResponseEntity<InputStreamResource> exportUsersToCSV() {
        log.info(" Export des utilisateurs en CSV");
        
        ByteArrayInputStream in = exportImportService.exportUsersToCSV();
        String filename = "utilisateurs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }


    @PostMapping("/users/import/excel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Importer les utilisateurs depuis Excel", description = "Importe des utilisateurs depuis un fichier Excel")
    public ResponseEntity<Map<String, Object>> importUsersFromExcel(@RequestParam("file") MultipartFile file) {
        log.info(" Import des utilisateurs depuis Excel: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier est vide"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier doit être au format Excel (.xlsx ou .xls)"));
        }

        Map<String, Object> result = exportImportService.importUsersFromExcel(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/users/import/csv")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Importer les utilisateurs depuis CSV", description = "Importe des utilisateurs depuis un fichier CSV")
    public ResponseEntity<Map<String, Object>> importUsersFromCSV(@RequestParam("file") MultipartFile file) {
        log.info(" Import des utilisateurs depuis CSV: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier est vide"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".csv")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier doit être au format CSV (.csv)"));
        }

        Map<String, Object> result = exportImportService.importUsersFromCSV(file);
        return ResponseEntity.ok(result);
    }

    //  EXPORT AGENCES 

    @GetMapping("/agences/export/excel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR', 'ROLE_OPERATEUR')")
    @Operation(summary = "Exporter les agences en Excel", description = "Télécharge un fichier Excel contenant toutes les agences")
    public ResponseEntity<InputStreamResource> exportAgencesToExcel() {
        log.info(" Export des agences en Excel");
        
        ByteArrayInputStream in = exportImportService.exportAgencesToExcel();
        String filename = "agences_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/agences/export/csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR', 'ROLE_OPERATEUR')")
    @Operation(summary = "Exporter les agences en CSV", description = "Télécharge un fichier CSV contenant toutes les agences")
    public ResponseEntity<InputStreamResource> exportAgencesToCSV() {
        log.info(" Export des agences en CSV");
        
        ByteArrayInputStream in = exportImportService.exportAgencesToCSV();
        String filename = "agences_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    // ==================== IMPORT AGENCES ====================

    @PostMapping("/agences/import/excel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Importer les agences depuis Excel", description = "Importe des agences depuis un fichier Excel")
    public ResponseEntity<Map<String, Object>> importAgencesFromExcel(@RequestParam("file") MultipartFile file) {
        log.info(" Import des agences depuis Excel: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier est vide"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier doit être au format Excel (.xlsx ou .xls)"));
        }

        Map<String, Object> result = exportImportService.importAgencesFromExcel(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/agences/import/csv")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Importer les agences depuis CSV", description = "Importe des agences depuis un fichier CSV")
    public ResponseEntity<Map<String, Object>> importAgencesFromCSV(@RequestParam("file") MultipartFile file) {
        log.info(" Import des agences depuis CSV: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier est vide"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".csv")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier doit être au format CSV (.csv)"));
        }

        Map<String, Object> result = exportImportService.importAgencesFromCSV(file);
        return ResponseEntity.ok(result);
    }

    // EXPORT COLIS 

    @GetMapping("/colis/export/excel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR', 'ROLE_OPERATEUR')")
    @Operation(summary = "Exporter les colis en Excel", description = "Télécharge un fichier Excel contenant tous les colis")
    public ResponseEntity<InputStreamResource> exportColisToExcel() {
        log.info(" Export des colis en Excel");
        
        ByteArrayInputStream in = exportImportService.exportColisToExcel();
        String filename = "colis_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/colis/export/csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR', 'ROLE_OPERATEUR')")
    @Operation(summary = "Exporter les colis en CSV", description = "Télécharge un fichier CSV contenant tous les colis")
    public ResponseEntity<InputStreamResource> exportColisToCSV() {
        log.info(" Export des colis en CSV");
        
        ByteArrayInputStream in = exportImportService.exportColisToCSV();
        String filename = "colis_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    // IMPORT COLIS 

    @PostMapping("/colis/import/excel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    @Operation(summary = "Importer les colis depuis Excel", description = "Importe des colis depuis un fichier Excel")
    public ResponseEntity<Map<String, Object>> importColisFromExcel(@RequestParam("file") MultipartFile file) {
        log.info(" Import des colis depuis Excel: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier est vide"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier doit être au format Excel (.xlsx ou .xls)"));
        }

        Map<String, Object> result = exportImportService.importColisFromExcel(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/colis/import/csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISEUR')")
    @Operation(summary = "Importer les colis depuis CSV", description = "Importe des colis depuis un fichier CSV")
    public ResponseEntity<Map<String, Object>> importColisFromCSV(@RequestParam("file") MultipartFile file) {
        log.info(" Import des colis depuis CSV: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier est vide"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".csv")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le fichier doit être au format CSV (.csv)"));
        }

        Map<String, Object> result = exportImportService.importColisFromCSV(file);
        return ResponseEntity.ok(result);
    }
}
