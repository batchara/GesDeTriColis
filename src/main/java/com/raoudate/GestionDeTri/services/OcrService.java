package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.dto.response.AgenceProche;
import com.raoudate.GestionDeTri.dto.response.CoordinatesDTO;
import com.raoudate.GestionDeTri.dto.response.DonneesStructureesDTO;
import com.raoudate.GestionDeTri.dto.response.GeocodingResultDTO;
import com.raoudate.GestionDeTri.dto.response.ScanColisResponseDTO;
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
        log.info(" Initialisation de Tesseract OCR");
        log.info(" Chemin tessdata: {}", tessdataPath);
        log.info(" Langue: {}", language);

        //  Configurer le chemin des bibliothèques natives Tesseract (macOS Homebrew)
        String libPath = "/opt/homebrew/lib";
        if (Files.exists(Path.of(libPath))) {
            System.setProperty("jna.library.path", libPath);
            log.info(" Chemin bibliothèques natives configuré: {}", libPath);
        }

        tesseract = new Tesseract();
        
        // Vérifier et configurer le chemin des données
        if (Files.exists(Path.of(tessdataPath))) {
            tesseract.setDatapath(tessdataPath);
            log.info(" Tessdata trouvé à: {}", tessdataPath);
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
                    log.info(" Tessdata trouvé à: {}", path);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                log.warn(" Tessdata non trouvé. OCR pourrait ne pas fonctionner correctement.");
            }
        }

        tesseract.setLanguage(language);
        // Optimisations pour vitesse (moins de 5 secondes)
        tesseract.setPageSegMode(3); // PSM_AUTO = 3 (plus rapide que 1)
        tesseract.setOcrEngineMode(1); // Neural nets LSTM engine only
        // Configurations pour accélérer (deprecated mais aucune alternative)
        @SuppressWarnings("deprecation")
        var suppressWarnings = new Object() {
            void call() {
                tesseract.setTessVariable("tessedit_char_blacklist", "");
                tesseract.setTessVariable("debug_file", "/dev/null");
            }
        };
        suppressWarnings.call();
        
        log.info(" Tesseract OCR initialisé (mode optimisé pour vitesse)");
    }

    /**
     * Extrait le texte d'une image (optimisé pour moins de 5 secondes)
     */
    public String extraireTexte(MultipartFile imageFile) throws Exception {
        log.info(" Extraction de texte depuis l'image: {}", imageFile.getOriginalFilename());
        long debut = System.currentTimeMillis();

        // Convertir MultipartFile en File temporaire
        File tempFile = File.createTempFile("ocr-", ".tmp");
        final File finalTempFile = tempFile;
        try {
            if (tempFile == null) {
                throw new IOException("Impossible de créer le fichier temporaire");
            }
            @SuppressWarnings("null")
            var suppressedTransfer = new Object() {
                void transfer() throws IOException {
                    imageFile.transferTo(finalTempFile);
                }
            };
            suppressedTransfer.transfer();
            
            // Lire et compresser l'image pour accélérer l'OCR
            BufferedImage image = ImageIO.read(tempFile);
            if (image == null) {
                throw new IOException("Impossible de lire l'image");
            }
            
            // Redimensionner l'image si elle est trop grande (max 1024x1024)
            int maxDimension = 1024;
            if (image.getWidth() > maxDimension || image.getHeight() > maxDimension) {
                float scale = Math.min(
                    (float) maxDimension / image.getWidth(),
                    (float) maxDimension / image.getHeight()
                );
                int newWidth = (int) (image.getWidth() * scale);
                int newHeight = (int) (image.getHeight() * scale);
                
                BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                resized.getGraphics().drawImage(image, 0, 0, newWidth, newHeight, null);
                image = resized;
                log.info(" Image redimensionnée: {}x{} -> {}x{}", 
                    imageFile.getOriginalFilename(), image.getWidth(), image.getHeight(), newWidth, newHeight);
            }

            // Extraire le texte (optimisé en mode rapide)
            String texte = tesseract.doOCR(image);
            long duree = System.currentTimeMillis() - debut;
            
            log.info(" Texte extrait ({} caractères en {}ms)", texte.length(), duree);
            if (duree > 5000) {
                log.warn(" OCR dépasse 5 secondes ({}ms)", duree);
            } else {
                log.info(" OCR rapide (<5s): {}ms", duree);
            }
            log.debug(" Texte: {}", texte);

            return texte;
        } finally {
            // Nettoyer le fichier temporaire
            if (finalTempFile != null && finalTempFile.exists()) {
                finalTempFile.delete();
            }
        }
    }

    /**
     * Extrait les coordonnées GPS (latitude, longitude) du texte OCR
     * 
     * @param texte Texte OCR brut
     * @return Tableau [latitude, longitude] ou null si non trouvé
     */
    private double[] extraireCoordonnees(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return null;
        }
        
        // Pattern pour latitude et longitude (format décimal)
        // Ex: "Latitude 6.15155" ou "@ Latitude {> Longitude\n6.15155 1.26717"
        Pattern patternCoords = Pattern.compile(
            "(latitude|lat)[^\\d]*([0-9]+\\.[0-9]+)[^\\d]*(longitude|lon|lng)[^\\d]*([0-9]+\\.[0-9]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        
        Matcher matcher = patternCoords.matcher(texte);
        if (matcher.find()) {
            try {
                double latitude = Double.parseDouble(matcher.group(2));
                double longitude = Double.parseDouble(matcher.group(4));
                
                // Valider que ce sont des coordonnées du Togo
                // Togo: Latitude 6° - 11° N, Longitude 0° - 2° E
                if (latitude >= 6.0 && latitude <= 11.0 && longitude >= 0.0 && longitude <= 2.0) {
                    log.info(" Coordonnées GPS extraites du texte: ({}, {})", latitude, longitude);
                    return new double[]{latitude, longitude};
                }
            } catch (NumberFormatException e) {
                log.warn(" Erreur lors du parsing des coordonnées: {}", e.getMessage());
            }
        }
        
        // Pattern alternatif : deux nombres décimaux consécutifs
        Pattern patternSimple = Pattern.compile("([0-9]+\\.[0-9]+)\\s+([0-9]+\\.[0-9]+)");
        Matcher matcherSimple = patternSimple.matcher(texte);
        
        while (matcherSimple.find()) {
            try {
                double val1 = Double.parseDouble(matcherSimple.group(1));
                double val2 = Double.parseDouble(matcherSimple.group(2));
                
                // Tester si c'est lat/lon ou lon/lat
                if (val1 >= 6.0 && val1 <= 11.0 && val2 >= 0.0 && val2 <= 2.0) {
                    log.info(" Coordonnées GPS extraites (pattern simple): ({}, {})", val1, val2);
                    return new double[]{val1, val2};
                } else if (val2 >= 6.0 && val2 <= 11.0 && val1 >= 0.0 && val1 <= 2.0) {
                    log.info(" Coordonnées GPS extraites (pattern simple inversé): ({}, {})", val2, val1);
                    return new double[]{val2, val1};
                }
            } catch (NumberFormatException e) {
                // Continuer la recherche
            }
        }
        
        log.info("ℹ Aucune coordonnée GPS trouvée dans le texte");
        return null;
    }

    /**
     * Détecte une adresse dans un texte
     * Recherche des patterns typiques d'adresses au Togo
     */
    public String detecterAdresse(String texte) {
        log.info(" Détection d'adresse dans le texte");

        if (texte == null || texte.trim().isEmpty()) {
            return null;
        }

        // Patterns pour détecter les adresses au Togo (PRIORITE 1)
        List<Pattern> patterns = Arrays.asList(
            // Label "Adresse:" suivi de la vraie adresse (PRIORITE MAXIMALE)
            // Capture tout jusqu'à la fin de ligne, y compris apostrophes et accents
            Pattern.compile("adresse\\s*:?\\s*(.+?)(?=\\n|$)", Pattern.CASE_INSENSITIVE),
            // Lieux typiques (GARE, MARCHE, MAISON, etc.)
            Pattern.compile("(GARE|MARCHE|MAISON|ROUTE)\\s+.+?(?=\\n|$)", Pattern.CASE_INSENSITIVE),
            // Quartier + Ville (ex: "Adidogome Lomé")
            Pattern.compile("([A-Za-zé]+\\s+Lomé|Lomé\\s+[A-Za-zé]+)", Pattern.CASE_INSENSITIVE),
            // Rue/Avenue + description
            Pattern.compile("(Rue|Avenue|Boulevard)\\s+[^\\n]{5,50}", Pattern.CASE_INSENSITIVE),
            // Villes principales du Togo
            Pattern.compile("(Lomé|Kara|Sokodé|Atakpamé|Kpalimé|Tsévié|Aného|Bassar|Dapaong|Niamtougou|Bafilo|Notsé|Vogan|Tabligbo|Tchamba)([^\\n]{0,30})", Pattern.CASE_INSENSITIVE),
            // BP + numéro
            Pattern.compile("BP\\s*\\d+", Pattern.CASE_INSENSITIVE),
            // Quartiers connus de Lomé et région maritime
            Pattern.compile("(Adidogome|Agoè|Nyékonakpoè|Bè|Tokoin|Amoutivé|Hédzranawoé|Démakpoè|Kagomé|Djidjolé|Kégué|Anfamé|Amadahomé)([^\\n]{0,30})", Pattern.CASE_INSENSITIVE)
        );

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(texte);
            if (matcher.find()) {
                String adresse = matcher.group().trim();
                
                // Si c'est le pattern avec "adresse:", extraire le groupe 1
                if (adresse.toLowerCase().startsWith("adresse") && matcher.groupCount() > 0) {
                    adresse = matcher.group(1).trim();
                }
                
                // Vérifier que ce n'est pas un symbole parasite et que c'est assez long
                if (!adresse.matches(".*[©@&=QILO{}].*") && adresse.length() > 2) {
                    log.info(" Adresse détectée: {}", adresse);
                    return adresse;
                }
            }
        }

        // Si aucun pattern ne correspond, prendre les lignes SANS symboles parasites
        String[] lignes = texte.split("\\n");
        StringBuilder adresse = new StringBuilder();
        int count = 0;
        for (String ligne : lignes) {
            ligne = ligne.trim();
            // Ignorer les lignes avec symboles parasites et les labels
            if (!ligne.isEmpty() 
                && ligne.length() > 3 
                && !ligne.matches(".*[©@&=QILO{}].*")
                && !ligne.toLowerCase().contains("détail")
                && !ligne.toLowerCase().contains("information")
                && !ligne.toLowerCase().contains("contact")
                && !ligne.toLowerCase().contains("localisation")) {
                
                if (count > 0) adresse.append(", ");
                adresse.append(ligne);
                count++;
                if (count >= 2) break; // Seulement 2 lignes max
            }
        }

        String result = adresse.toString();
        if (!result.isEmpty()) {
            log.info(" Adresse approximative détectée: {}", result);
            return result;
        }

        log.warn(" Aucune adresse détectée dans le texte");
        return null;
    }

    /**
     * Scan complet du bordereau d'un colis : OCR + Parsing IA + Géocodage + Recherche d'agence
     * 
     * Processus intelligent :
     * 1. Extraction OCR (Tesseract) du texte du bordereau
     * 2. Parsing IA (OpenAI) pour structurer les données (nom, adresse, téléphone, région)
     * 3. Géocodage de l'adresse (Google Maps Geocoding API)
     * 4. Recherche des agences les plus proches avec distances routières réelles (Google Distance Matrix API)
     */
    public ScanColisResponseDTO scannerColis(MultipartFile image, String adresseManuelle) {
        log.info(" Début du scan de bordereau");

        ScanColisResponseDTO.ScanColisResponseDTOBuilder response = ScanColisResponseDTO.builder();

        try {
            String adresseFinale = adresseManuelle;
            String texteExtrait = null;
            boolean ocrSucces = false;
            DonneesStructureesDTO donneesStructurees = null;
            double[] coordonneesGPS = null; // GPS extraites du texte OCR

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
                    log.info(" Texte extrait: {}", texteExtrait);
                    
                    // PRIORITE 1: Extraire les coordonnées GPS si présentes
                    coordonneesGPS = extraireCoordonnees(texteExtrait);
                    if (coordonneesGPS != null) {
                        log.info(" Coordonnées GPS extraites du bordereau: ({}, {})", 
                            coordonneesGPS[0], coordonneesGPS[1]);
                    }
                    
                    // 🤖 Parsing intelligent avec OpenAI
                    log.info("🤖 Analyse intelligente du texte avec OpenAI");
                    donneesStructurees = openAiParsingService != null ? openAiParsingService.parserTexteOcr(texteExtrait) : null;
                    
                    if (donneesStructurees != null) {
                        log.info(" Données structurées: nom={}, code={}, region={}, adresse={}", 
                            donneesStructurees.getNom(),
                            donneesStructurees.getCode(),
                            donneesStructurees.getRegion(),
                            donneesStructurees.getAdresse());
                    }
                    
                    // Utiliser l'adresse structurée si valide
                    if (donneesStructurees != null && donneesStructurees.getAdresse() != null 
                        && !donneesStructurees.getAdresse().trim().isEmpty()
                        && !donneesStructurees.getAdresse().contains("©")
                        && !donneesStructurees.getAdresse().contains("@")) {
                        
                        adresseFinale = donneesStructurees.getAdresse();
                        log.info(" Adresse structurée par IA: {}", adresseFinale);
                        
                        // Améliorer l'adresse avec le nom de l'agence si disponible
                        if (donneesStructurees.getNom() != null && !donneesStructurees.getNom().trim().isEmpty()) {
                            adresseFinale = donneesStructurees.getNom() + ", " + adresseFinale;
                        }
                        
                    } else {
                        // Fallback sur la détection basique
                        adresseFinale = detecterAdresse(texteExtrait);
                        log.info(" Adresse détectée (fallback): {}", adresseFinale);
                    }
                    
                    ocrSucces = true;
                } catch (Exception e) {
                    log.error(" Erreur lors de l'OCR", e);
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

            // NOUVELLE LOGIQUE: Si coordonnées GPS extraites, utiliser directement
            GeocodingResultDTO geocoding;
            
            if (coordonneesGPS != null) {
                // Utiliser les coordonnées GPS du bordereau directement
                log.info(" Utilisation des coordonnées GPS extraites: ({}, {})", 
                    coordonneesGPS[0], coordonneesGPS[1]);
                
                geocoding = GeocodingResultDTO.builder()
                    .succes(true)
                    .adresseOriginale(adresseFinale != null ? adresseFinale : "Coordonnées GPS du bordereau")
                    .adresseFormattee("GPS exact: " + coordonneesGPS[0] + ", " + coordonneesGPS[1])
                    .coordonnees(new CoordinatesDTO(coordonneesGPS[0], coordonneesGPS[1]))
                    .build();
                    
            } else {
                // Fallback sur le géocodage si pas de GPS
                if (adresseFinale == null || adresseFinale.trim().isEmpty()) {
                    return response
                            .succes(false)
                            .message("Aucune adresse ni coordonnées GPS détectées dans l'image")
                            .build();
                }
                
                // Récupérer la région AVANT le géocodage pour enrichir l'adresse
                String regionDetectee = (donneesStructurees != null) ? donneesStructurees.getRegion() : null;
                
                log.info(" Géocodage de l'adresse (pas de GPS dans le bordereau): {} - Région détectée: {}", 
                    adresseFinale, regionDetectee);
                geocoding = geocodingService.geocodeAdresse(adresseFinale, regionDetectee);
            }
            
            response.geocodage(geocoding);

            if (!geocoding.getSucces()) {
                return response
                        .succes(false)
                        .message("Adresse détectée mais impossible à géocoder")
                        .messageErreur(geocoding.getMessageErreur())
                        .build();
            }

            // Recherche de l'agence la plus proche
            // Important : Filtrer d'abord par région si détectée
            String regionDetectee = (donneesStructurees != null) ? donneesStructurees.getRegion() : null;
            
            log.info(" Recherche de l'agence la plus proche - Région détectée: {}", regionDetectee);
            
            List<AgenceProche> agencesProches = geocodingService.trouverAgencesProchesParRegion(
                    geocoding.getCoordonnees().getLatitude(),
                    geocoding.getCoordonnees().getLongitude(),
                    6, // Top 6 des agences les plus proches
                    regionDetectee // Filtrer par région en priorité
            );

            if (agencesProches.isEmpty()) {
                return response
                        .succes(false)
                        .message("Aucune agence trouvée")
                        .build();
            }

            AgenceProche agenceLaPlusProche = agencesProches.get(0);
            
            // VERIFICATION: Si l'agence la plus proche est > 15 km ET qu'une région était détectée,
            // chercher dans TOUTES les régions pour voir s'il y a une agence plus proche ailleurs
            if (regionDetectee != null && agenceLaPlusProche.getDistanceKm() > 15.0) {
                log.warn(" Agence la plus proche à {} km (région {}), recherche dans toutes les régions...", 
                    agenceLaPlusProche.getDistanceKm(), regionDetectee);
                
                List<AgenceProche> agencesToutesRegions = geocodingService.trouverAgencesProchesParRegion(
                        geocoding.getCoordonnees().getLatitude(),
                        geocoding.getCoordonnees().getLongitude(),
                        6,
                        null // Chercher dans TOUTES les régions
                );
                
                if (!agencesToutesRegions.isEmpty() && 
                    agencesToutesRegions.get(0).getDistanceKm() < agenceLaPlusProche.getDistanceKm()) {
                    
                    AgenceProche agencePlusProche = agencesToutesRegions.get(0);
                    log.info(" Agence plus proche trouvée dans région {} : {} ({} km au lieu de {} km)", 
                        agencePlusProche.getRegion(), 
                        agencePlusProche.getNom(),
                        agencePlusProche.getDistanceKm(),
                        agenceLaPlusProche.getDistanceKm());
                    
                    agencesProches = agencesToutesRegions;
                    agenceLaPlusProche = agencePlusProche;
                }
            }
            
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
            log.error(" Erreur lors du scan du colis", e);
            return response
                    .succes(false)
                    .messageErreur("Erreur interne: " + e.getMessage())
                    .build();
        }
    }
}
