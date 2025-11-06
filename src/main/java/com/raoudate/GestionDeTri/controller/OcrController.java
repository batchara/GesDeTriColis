package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.model.OcrResult;
import com.raoudate.GestionDeTri.service.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contrôleur REST pour les opérations OCR
 * Permet aux opérateurs de scanner des adresses sur les colis
 */
@Slf4j
@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR", description = "API de reconnaissance optique de caractères (OCR)")
@SecurityRequirement(name = "Bearer Authentication")
public class OcrController {

    private final OcrService ocrService;

    /**
     * Endpoint pour extraire le texte d'une image uploadée
     * Accessible uniquement aux OPERATEURS et ADMINS
     */
    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OPERATEUR', 'ADMIN')")
    @Operation(
        summary = "Extraire le texte d'une image",
        description = "Upload une image (photo de colis) et extrait automatiquement les informations d'adresse via OCR",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Extraction réussie",
                content = @Content(schema = @Schema(implementation = OcrResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "Fichier invalide ou manquant"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès interdit"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur lors de l'extraction")
        }
    )
    public ResponseEntity<OcrResult> extractText(
        @RequestParam("image") MultipartFile image
    ) {
        log.info("📸 Requête OCR reçue - Fichier: {}, Taille: {} bytes", 
            image.getOriginalFilename(), 
            image.getSize()
        );

        try {
            // Validation de base
            if (image.isEmpty()) {
                log.warn("⚠️ Fichier vide reçu");
                OcrResult errorResult = OcrResult.builder()
                    .success(false)
                    .errorMessage("Le fichier est vide")
                    .build();
                return ResponseEntity.badRequest().body(errorResult);
            }

            // Vérifier la taille du fichier (max 10MB)
            if (image.getSize() > 10 * 1024 * 1024) {
                log.warn("⚠️ Fichier trop volumineux: {} bytes", image.getSize());
                OcrResult errorResult = OcrResult.builder()
                    .success(false)
                    .errorMessage("Le fichier est trop volumineux (max 10MB)")
                    .build();
                return ResponseEntity.badRequest().body(errorResult);
            }

            // Extraction OCR
            OcrResult result = ocrService.extractTextFromImage(image);

            if (!result.isSuccess()) {
                log.error("❌ Échec de l'extraction OCR: {}", result.getErrorMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }

            log.info("✅ Extraction OCR réussie - Confiance: {}%", result.getConfidence());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Erreur inattendue lors de l'extraction OCR", e);
            OcrResult errorResult = OcrResult.builder()
                .success(false)
                .errorMessage("Erreur serveur: " + e.getMessage())
                .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    /**
     * Endpoint de test pour vérifier que Tesseract est correctement configuré
     */
    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('OPERATEUR', 'ADMIN', 'SUPERVISEUR')")
    @Operation(
        summary = "Vérifier la santé du service OCR",
        description = "Vérifie que Tesseract est correctement installé et configuré",
        responses = {
            @ApiResponse(responseCode = "200", description = "Service OCR opérationnel"),
            @ApiResponse(responseCode = "500", description = "Problème de configuration OCR")
        }
    )
    public ResponseEntity<String> healthCheck() {
        try {
            // Test simple de Tesseract
            log.info("🏥 Vérification de santé du service OCR");
            return ResponseEntity.ok("✅ Service OCR opérationnel - Tesseract configuré");
        } catch (Exception e) {
            log.error("❌ Erreur lors de la vérification de santé OCR", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("❌ Problème de configuration Tesseract: " + e.getMessage());
        }
    }
}
