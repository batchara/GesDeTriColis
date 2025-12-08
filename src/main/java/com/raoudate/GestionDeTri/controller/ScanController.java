package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.dto.response.AgenceProche;
import com.raoudate.GestionDeTri.dto.response.GeocodingResultDTO;
import com.raoudate.GestionDeTri.dto.response.ScanColisResponseDTO;
import com.raoudate.GestionDeTri.services.GeocodingService;
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

import java.util.List;

/**
 * Controller pour le système OCR et de géocodage
 * Accessible uniquement aux Opérateurs, Superviseurs et Admins
 */
@RestController
@RequestMapping("/scan")
@Tag(name = "OCR & Géocodage", description = "API pour scanner les colis et assigner automatiquement les agences")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('OPERATEUR', 'SUPERVISEUR', 'ADMIN')")
public class ScanController {

    private final OcrService ocrService;
    private final GeocodingService geocodingService;

    @PostMapping("/colis")
    @Operation(summary = "Scanner le bordereau d'un colis", 
               description = "Extrait les informations du bordereau via OCR (nom, adresse, téléphone, région), géocode l'adresse et trouve l'agence la plus proche avec distances routières réelles")
    public ResponseEntity<ScanColisResponseDTO> scannerColis(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "adresse", required = false) String adresseManuelle) {
        
        log.info(" Requête de scan de bordereau reçue");
        
        ScanColisResponseDTO response = ocrService.scannerColis(image, adresseManuelle);
        
        HttpStatus status = response.getSucces() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/geocode")
    @Operation(summary = "Géocoder une adresse", 
               description = "Convertit une adresse textuelle en coordonnées GPS")
    public ResponseEntity<GeocodingResultDTO> geocoderAdresse(
            @RequestParam("adresse") String adresse) {
        
        log.info(" Requête de géocodage: {}", adresse);
        
        GeocodingResultDTO result = geocodingService.geocodeAdresse(adresse);
        
        HttpStatus status = result.getSucces() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    @GetMapping("/agence-proche")
    @Operation(summary = "Trouver l'agence la plus proche", 
               description = "Trouve l'agence la plus proche de coordonnées GPS données")
    public ResponseEntity<AgenceProche> trouverAgenceLaPlusProche(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude) {
        
        log.info(" Recherche agence proche de ({}, {})", latitude, longitude);
        
        AgenceProche agence = geocodingService.trouverAgenceLaPlusProche(latitude, longitude);
        
        if (agence == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(agence);
    }

    @GetMapping("/agences-proches")
    @Operation(summary = "Trouver les N agences les plus proches", 
               description = "Trouve les N agences les plus proches de coordonnées GPS données")
    public ResponseEntity<List<AgenceProche>> trouverAgencesProches(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "nombre", defaultValue = "5") int nombre) {
        
        log.info(" Recherche des {} agences proches de ({}, {})", nombre, latitude, longitude);
        
        List<AgenceProche> agences = geocodingService.trouverAgencesProches(latitude, longitude, nombre);
        
        return ResponseEntity.ok(agences);
    }

    @PostMapping("/ocr")
    @Operation(summary = "Extraire les informations d'un bordereau (OCR)", 
               description = "Utilise Tesseract OCR + OpenAI pour extraire le texte et parser les informations du bordereau (nom, adresse, téléphone, région, code postal)")
    public ResponseEntity<?> extraireTexte(
            @RequestParam("image") MultipartFile image) {
        
        log.info(" Requête d'extraction de bordereau");
        
        try {
            ScanColisResponseDTO response = ocrService.scannerColis(image, null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(" Erreur lors du scan OCR + parsing IA", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }

    // Classe interne pour la réponse OCR simple
    record OcrResponse(String texteExtrait, String adresseDetectee) {}
}
