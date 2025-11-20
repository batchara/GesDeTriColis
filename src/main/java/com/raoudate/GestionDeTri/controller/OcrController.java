package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.Dto.OcrResultDTO;
import com.raoudate.GestionDeTri.services.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller pour l'extraction de texte OCR simple
 * Utilisé pour extraire le code bureau des images
 */
@RestController
@RequestMapping("/ocr")
@Tag(name = "OCR", description = "API pour l'extraction simple de texte OCR")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('OPERATEUR', 'SUPERVISEUR', 'ADMIN')")
public class OcrController {

    private final OcrService ocrService;

    /**
     * Extrait le texte brut d'une image via OCR
     * Utilisé pour extraire le code bureau des images de boîtes postales
     */
    @PostMapping("/extract")
    @Operation(summary = "Extraire le texte d'une image", 
               description = "Extrait le texte brut d'une image via Tesseract OCR")
    public ResponseEntity<OcrResultDTO> extractText(
            @RequestParam("image") MultipartFile image) {
        
        try {
            log.info("📸 Requête d'extraction OCR reçue - Fichier: {}, Taille: {} bytes", 
                    image.getOriginalFilename(), image.getSize());
            
            // Extraire le texte via Tesseract
            String texteExtrait = ocrService.extraireTexte(image);
            
            log.info("✅ Texte extrait avec succès - {} caractères", texteExtrait.length());
            
            // Retourner le résultat
            OcrResultDTO result = OcrResultDTO.builder()
                    .succes(true)
                    .texte(texteExtrait)
                    .message("Texte extrait avec succès")
                    .build();
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'extraction OCR: {}", e.getMessage(), e);
            
            OcrResultDTO result = OcrResultDTO.builder()
                    .succes(false)
                    .texte("")
                    .message("Erreur lors de l'extraction OCR: " + e.getMessage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
