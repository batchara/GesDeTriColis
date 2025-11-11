package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.Dto.AgenceProche;
import com.raoudate.GestionDeTri.Dto.DonneesStructureesDTO;
import com.raoudate.GestionDeTri.Dto.GeocodingResultDTO;
import com.raoudate.GestionDeTri.Dto.ScanColisResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Service OCR pour extraire les adresses des images de colis
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OcrService {

    @Value("${ocr.tesseract.datapath:/opt/homebrew/share/tessdata}")
    private String tessdataPath;

    @Value("${ocr.tesseract.language:fra}")
    private String language;

    private final GeocodingService geocodingService;
    private final OpenAiParsingService openAiParsingService;
    private ITesseract tesseract;

    @PostConstruct
    public void init() {
        log.info("🔧 Initialisation de Tesseract OCR");
        log.info("📁 Chemin tessdata: {}", tessdataPath);
        log.info("🌍 Langue: {}", language);

        // 🔧 Configurer le chemin des bibliothèques natives Tesseract (macOS Homebrew)
        String libPath = "/opt/homebrew/lib";
        if (Files.exists(Path.of(libPath))) {
            System.setProperty("jna.library.path", libPath);
            log.info("📚 Chemin bibliothèques natives configuré: {}", libPath);
        }

        tesseract = new Tesseract();
        
        // Vérifier et configurer le chemin des données
        if (Files.exists(Path.of(tessdataPath))) {
            tesseract.setDatapath(tessdataPath);
            log.info("✅ Tessdata trouvé à: {}", tessdataPath);
        } else {
            // Essayer des chemins alternatifs
            String[] alternatePaths = {
                "/usr/local/share/tessdata",
                "/opt/homebrew/share/tessdata",
                "/usr/share/tessdata",
                System.getProperty("user.home") + "/tessdata"
            };
            
            boolean found = false;
            for (String path : alternatePaths) {
                if (Files.exists(Path.of(path))) {
                    tesseract.setDatapath(path);
                    log.info("✅ Tessdata trouvé à: {}", path);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                log.warn("⚠️ Tessdata non trouvé. OCR pourrait ne pas fonctionner correctement.");
            }
        }

        tesseract.setLanguage(language);
        tesseract.setPageSegMode(1); // Automatic page segmentation with OSD
        tesseract.setOcrEngineMode(1); // Neural nets LSTM engine only
        
        log.info("✅ Tesseract OCR initialisé");
    }

    /**
     * Extrait le texte d'une image
     */
    public String extraireTexte(MultipartFile imageFile) throws Exception {
        log.info("📸 Extraction de texte depuis l'image: {}", imageFile.getOriginalFilename());

        // Convertir MultipartFile en File temporaire
        File tempFile = File.createTempFile("ocr-", ".tmp");
        try {
            imageFile.transferTo(tempFile);
            
            // Lire l'image
            BufferedImage image = ImageIO.read(tempFile);
            if (image == null) {
                throw new IOException("Impossible de lire l'image");
            }

            // Extraire le texte
            String texte = tesseract.doOCR(image);
            log.info("✅ Texte extrait ({} caractères)", texte.length());
            log.debug("📝 Texte: {}", texte);

            return texte;
        } finally {
            // Nettoyer le fichier temporaire
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Détecte une adresse dans un texte
     * Recherche des patterns typiques d'adresses au Togo
     */
    public String detecterAdresse(String texte) {
        log.info("🔍 Détection d'adresse dans le texte");

        if (texte == null || texte.trim().isEmpty()) {
            return null;
        }

        // Patterns pour détecter les adresses au Togo
        List<Pattern> patterns = Arrays.asList(
            // Quartier + Ville (ex: "Adidogome Lomé")
            Pattern.compile("([A-Za-zé]+\\s+Lomé|Lomé\\s+[A-Za-zé]+)", Pattern.CASE_INSENSITIVE),
            // Rue/Avenue + numéro (ex: "Avenue de la Paix 123")
            Pattern.compile("(Rue|Avenue|Boulevard)\\s+[^\\n]{5,50}", Pattern.CASE_INSENSITIVE),
            // Villes principales du Togo
            Pattern.compile("(Lomé|Kara|Sokodé|Atakpamé|Kpalimé|Tsévié|Aného|Bassar|Dapaong|Niamtougou|Bafilo|Notsé|Vogan|Tabligbo|Tchamba)([^\\n]{0,30})", Pattern.CASE_INSENSITIVE),
            // BP + numéro
            Pattern.compile("BP\\s*\\d+", Pattern.CASE_INSENSITIVE),
            // Quartiers connus de Lomé
            Pattern.compile("(Adidogome|Agoè|Nyékonakpoè|Bè|Tokoin|Amoutivé|Hédzranawoé|Démakpoè|Kagomé|Djidjolé|Kégué|Anfamé)([^\\n]{0,30})", Pattern.CASE_INSENSITIVE)
        );

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(texte);
            if (matcher.find()) {
                String adresse = matcher.group().trim();
                log.info("✅ Adresse détectée: {}", adresse);
                return adresse;
            }
        }

        // Si aucun pattern ne correspond, prendre les 3 premières lignes non vides
        String[] lignes = texte.split("\\n");
        StringBuilder adresse = new StringBuilder();
        int count = 0;
        for (String ligne : lignes) {
            ligne = ligne.trim();
            if (!ligne.isEmpty() && ligne.length() > 3) {
                if (count > 0) adresse.append(", ");
                adresse.append(ligne);
                count++;
                if (count >= 3) break;
            }
        }

        String result = adresse.toString();
        if (!result.isEmpty()) {
            log.info("✅ Adresse approximative détectée: {}", result);
            return result;
        }

        log.warn("⚠️ Aucune adresse détectée dans le texte");
        return null;
    }

    /**
     * Scan complet d'un colis : OCR + Géocodage + Recherche d'agence
     */
    public ScanColisResponseDTO scannerColis(MultipartFile image, String adresseManuelle) {
        log.info("📦 Début du scan de colis");

        ScanColisResponseDTO.ScanColisResponseDTOBuilder response = ScanColisResponseDTO.builder();

        try {
            String adresseFinale = adresseManuelle;
            String texteExtrait = null;
            boolean ocrSucces = false;
            DonneesStructureesDTO donneesStructurees = null;

            // Si pas d'adresse manuelle, utiliser l'OCR
            if (adresseManuelle == null || adresseManuelle.trim().isEmpty()) {
                if (image == null) {
                    return response
                            .succes(false)
                            .messageErreur("Aucune image ni adresse fournie")
                            .build();
                }

                try {
                    texteExtrait = extraireTexte(image);
                    log.error("DEBUG - openAiParsingService null check: {} | texteExtrait length: {}", openAiParsingService == null, texteExtrait != null ? texteExtrait.length() : 0);
                    // 🤖 Parsing intelligent avec OpenAI
                    log.info("🤖 Analyse intelligente du texte avec OpenAI");
                    donneesStructurees = openAiParsingService != null ? openAiParsingService.parserTexteOcr(texteExtrait) : null;
                    
                    // Utiliser l'adresse structurée si disponible
                    if (donneesStructurees != null && donneesStructurees.getAdresse() != null) {
                        adresseFinale = donneesStructurees.getAdresse();
                        log.info("✅ Adresse structurée par IA: {}", adresseFinale);
                    } else {
                        // Fallback sur la détection basique
                        adresseFinale = detecterAdresse(texteExtrait);
                        log.info("📍 Adresse détectée (fallback): {}", adresseFinale);
                    }
                    
                    ocrSucces = true;
                } catch (Exception e) {
                    log.error("❌ Erreur lors de l'OCR", e);
                    return response
                            .succes(false)
                            .ocrSucces(false)
                            .messageErreur("Erreur lors de l'extraction du texte: " + e.getMessage())
                            .build();
                }
            }

            response.texteExtrait(texteExtrait)
                    .adresseDetectee(adresseFinale)
                    .donneesStructurees(donneesStructurees)
                    .ocrSucces(ocrSucces);

            // Si aucune adresse n'a été détectée
            if (adresseFinale == null || adresseFinale.trim().isEmpty()) {
                return response
                        .succes(false)
                        .message("Aucune adresse détectée dans l'image")
                        .build();
            }

            // Géocodage de l'adresse
            log.info("🗺️ Géocodage de l'adresse: {}", adresseFinale);
            GeocodingResultDTO geocoding = geocodingService.geocodeAdresse(adresseFinale);
            response.geocodage(geocoding);

            if (!geocoding.getSucces()) {
                return response
                        .succes(false)
                        .message("Adresse détectée mais impossible à géocoder")
                        .messageErreur(geocoding.getMessageErreur())
                        .build();
            }

            // Recherche de l'agence la plus proche
            log.info("🏢 Recherche de l'agence la plus proche");
            List<AgenceProche> agencesProches = geocodingService.trouverAgencesProches(
                    geocoding.getCoordonnees().getLatitude(),
                    geocoding.getCoordonnees().getLongitude(),
                    6 // Top 6 des agences les plus proches
            );

            if (agencesProches.isEmpty()) {
                return response
                        .succes(false)
                        .message("Aucune agence trouvée")
                        .build();
            }

            AgenceProche agenceLaPlusProche = agencesProches.get(0);
            List<AgenceProche> autresAgences = agencesProches.size() > 1 
                    ? agencesProches.subList(1, agencesProches.size()) 
                    : List.of();

            return response
                    .agenceLaPlusProche(agenceLaPlusProche)
                    .autresAgencesProches(autresAgences)
                    .succes(true)
                    .message(String.format("Colis assigné à l'agence %s (%s km)", 
                            agenceLaPlusProche.getNom(), 
                            agenceLaPlusProche.getDistanceKm()))
                    .build();

        } catch (Exception e) {
            log.error("❌ Erreur lors du scan du colis", e);
            return response
                    .succes(false)
                    .messageErreur("Erreur interne: " + e.getMessage())
                    .build();
        }
    }
}
