package com.raoudate.GestionDeTri.service;

import com.raoudate.GestionDeTri.model.OcrResult;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service de reconnaissance OCR utilisant Tesseract
 * Permet d'extraire du texte d'images et d'identifier automatiquement les adresses
 */
@Slf4j
@Service
public class OcrService {

    @Value("${ocr.tesseract.datapath:/usr/local/share/tessdata}")
    private String tessDataPath;

    @Value("${ocr.tesseract.language:fra}")
    private String ocrLanguage;

    /**
     * Extrait le texte d'une image uploadée avec pré-traitement
     */
    public OcrResult extractTextFromImage(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("🔍 Début de l'extraction OCR pour le fichier: {}", file.getOriginalFilename());
            
            // Validation du fichier
            if (file.isEmpty()) {
                return createErrorResult("Le fichier est vide", startTime);
            }

            if (!isImageFile(file)) {
                return createErrorResult("Le fichier n'est pas une image valide", startTime);
            }

            // Lire l'image depuis les bytes
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (image == null) {
                return createErrorResult("Impossible de lire l'image", startTime);
            }

            // Configuration de Tesseract pour MAXIMUM DE PRÉCISION
            Tesseract tesseract = new Tesseract();
            
            // Utiliser l'image ORIGINALE (meilleur pour captures d'écran et photos de qualité)
            BufferedImage processedImage = image;
            
            // Vérifier et configurer le chemin des données
            File tessDataDir = new File(tessDataPath);
            if (!tessDataDir.exists()) {
                log.warn("⚠️ Le répertoire tessdata n'existe pas: {}. Utilisation de la configuration par défaut.", tessDataPath);
            } else {
                tesseract.setDatapath(tessDataPath);
                log.info("✅ Tesseract datapath configuré: {}", tessDataPath);
            }
            
            tesseract.setLanguage(ocrLanguage);
            
            // ⭐ CONFIGURATION OPTIMALE POUR TEXTE DIGITAL (captures d'écran, photos de qualité) ⭐
            tesseract.setPageSegMode(3); // Fully automatic page segmentation, but no OSD (meilleur pour texte mixte)
            tesseract.setOcrEngineMode(1); // Neural nets LSTM engine only (le plus précis)
            
            log.info("🎯 Configuration Tesseract : PSM=3 (auto sans rotation), OEM=1 (LSTM), Image originale non traitée");

            // Extraction du texte avec l'image pré-traitée
            String rawText = tesseract.doOCR(processedImage);
            
            // Nettoyer le texte : garder uniquement lettres, chiffres, espaces et ponctuation de base
            String cleanedText = cleanOcrText(rawText);
            
            log.info("📄 Texte brut extrait ({} caractères):\n{}", 
                    rawText != null ? rawText.length() : 0, rawText);
            log.info("🧹 Texte nettoyé ({} caractères):\n{}", 
                    cleanedText != null ? cleanedText.length() : 0, cleanedText);

            // Extraire UNIQUEMENT l'adresse du bordereau scanné
            Map<String, String> extractedFields = extractFieldsFromBordereau(cleanedText);
            String adresseExtraite = extractedFields.get("adresse");
            
            log.info("� Adresse extraite: {}", adresseExtraite);

            // Retourner UNIQUEMENT l'adresse extraite
            OcrResult result = OcrResult.builder()
                .rawText(cleanedText != null ? cleanedText.trim() : "")
                .success(adresseExtraite != null && !adresseExtraite.isEmpty())
                .confidence(adresseExtraite != null && adresseExtraite.length() > 10 ? 98 : 60)
                .processingTime(System.currentTimeMillis() - startTime)
                .errorMessage(adresseExtraite == null ? "Aucune adresse détectée dans l'image" : null)
                // UNIQUEMENT l'adresse - tous les autres champs à null
                .adresse(adresseExtraite)
                .codeSuivi(null)
                .nomExpediteur(null)
                .nomDestinataire(null)
                .telephone(null)
                .poids(null)
                .ville(null)
                .codePostal(null)
                .pays(null)
                .build();
            
            log.info("✅ Extraction OCR terminée en {}ms - Adresse: {} - Confiance: {}%", 
                    result.getProcessingTime(), 
                    adresseExtraite != null ? adresseExtraite : "NON TROUVÉE",
                    result.getConfidence());
            
            return result;

        } catch (TesseractException e) {
            log.error("❌ Erreur Tesseract lors de l'extraction: {}", e.getMessage(), e);
            return createErrorResult("Erreur lors de la reconnaissance OCR: " + e.getMessage(), startTime);
        } catch (IOException e) {
            log.error("❌ Erreur I/O lors de la lecture de l'image: {}", e.getMessage(), e);
            return createErrorResult("Erreur lors de la lecture de l'image: " + e.getMessage(), startTime);
        } catch (Exception e) {
            log.error("❌ Erreur inattendue lors de l'extraction OCR: {}", e.getMessage(), e);
            return createErrorResult("Erreur inattendue: " + e.getMessage(), startTime);
        }
    }

    /**
     * Nettoie le texte OCR pour ne garder que les lettres, chiffres et ponctuation de base.
     * Supprime les icônes, emojis et caractères spéciaux.
     * 
     * @param rawText Texte brut extrait par Tesseract
     * @return Texte nettoyé sans icônes ni caractères spéciaux
     */
    private String cleanOcrText(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return rawText;
        }
        
        // Pattern pour garder uniquement:
        // - Lettres (Unicode \p{L} incluant les accents français: é, è, à, ç, ê, ô, û, etc.)
        // - Chiffres (\p{N})
        // - Espaces (\s)
        // - Ponctuation de base: . , ; : ( ) / - + ' "
        // - Sauts de ligne
        String cleaned = rawText.replaceAll("[^\\p{L}\\p{N}\\s.,;:()/\\-+'\"\\r\\n]", "");
        
        // Supprimer les lignes vides multiples
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");
        
        // Supprimer les espaces multiples
        cleaned = cleaned.replaceAll(" {2,}", " ");
        
        return cleaned.trim();
    }

    /**
     * Extrait UNIQUEMENT le champ Adresse de la section DESTINATAIRE du bordereau scanné.
     * Cherche "Adresse :" et extrait la valeur qui se trouve À DROITE (après les deux-points).
     * 
     * @param text Texte nettoyé extrait par OCR
     * @return Map contenant uniquement l'adresse extraite
     */
    private Map<String, String> extractFieldsFromBordereau(String text) {
        Map<String, String> fields = new HashMap<>();
        
        if (text == null || text.isEmpty()) {
            return fields;
        }
        
        log.info("🔍 Extraction de l'adresse de la section DESTINATAIRE...");
        
        // Pattern pour extraire l'adresse dans la section DESTINATAIRE
        // Cherche "Adresse:" puis capture la PREMIÈRE ligne non-vide qui suit
        Pattern adressePattern = Pattern.compile("Adresse\\s*:\\s*\\n?\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher adresseMatcher = adressePattern.matcher(text);
        
        if (adresseMatcher.find()) {
            String adresse = adresseMatcher.group(1).trim();
            // Nettoyer l'adresse (enlever les espaces multiples)
            adresse = adresse.replaceAll("\\s+", " ");
            
            if (!adresse.toLowerCase().contains("non sp") && adresse.length() > 3) {
                fields.put("adresse", adresse);
                log.info("✅ Adresse extraite: '{}'", adresse);
            } else {
                log.warn("⚠️ Adresse non valide ou vide: '{}'", adresse);
            }
        } else {
            log.warn("⚠️ Aucune adresse trouvée dans le texte");
            log.debug("Texte analysé: {}", text);
        }
        
        return fields;
    }

    /**
     * Pré-traitement de l'image pour MAXIMISER la précision OCR
     * (Actuellement non utilisé - on utilise l'image originale)
     */
    @SuppressWarnings("unused")
    private BufferedImage preprocessImage(BufferedImage original) {
        log.info("🎨 Pré-traitement haute qualité de l'image...");
        
        int width = original.getWidth();
        int height = original.getHeight();
        
        // 1. Agrandissement 2x pour améliorer la résolution (meilleure reconnaissance)
        int scaledWidth = width * 2;
        int scaledHeight = height * 2;
        BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(original, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        
        log.info("📐 Image agrandie: {}x{} → {}x{}", width, height, scaledWidth, scaledHeight);
        
        // 2. Conversion en niveaux de gris
        BufferedImage grayscale = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = grayscale.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        
        // 3. Binarisation adaptative (Otsu's method approximation)
        // Calculer le seuil optimal automatiquement
        int[] histogram = new int[256];
        for (int y = 0; y < scaledHeight; y++) {
            for (int x = 0; x < scaledWidth; x++) {
                int rgb = grayscale.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;
                histogram[gray]++;
            }
        }
        
        int threshold = calculateOtsuThreshold(histogram, scaledWidth * scaledHeight);
        log.info("🎯 Seuil de binarisation calculé: {}", threshold);
        
        // Appliquer la binarisation
        BufferedImage binary = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < scaledHeight; y++) {
            for (int x = 0; x < scaledWidth; x++) {
                int rgb = grayscale.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;
                int newColor = gray >= threshold ? 0xFFFFFFFF : 0xFF000000;
                binary.setRGB(x, y, newColor);
            }
        }
        
        log.info("✅ Pré-traitement haute qualité terminé");
        return binary;
    }
    
    /**
     * Calcule le seuil optimal en utilisant la méthode d'Otsu
     */
    private int calculateOtsuThreshold(int[] histogram, int totalPixels) {
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }
        
        float sumB = 0;
        int wB = 0;
        int wF = 0;
        float maxVariance = 0;
        int threshold = 0;
        
        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            
            wF = totalPixels - wB;
            if (wF == 0) break;
            
            sumB += i * histogram[i];
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;
            
            float variance = wB * wF * (mB - mF) * (mB - mF);
            
            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = i;
            }
        }
        
        return threshold;
    }

    /**
     * Vérifie si le fichier est une image
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Crée un résultat d'erreur
     */
    private OcrResult createErrorResult(String errorMessage, long startTime) {
        return OcrResult.builder()
            .success(false)
            .errorMessage(errorMessage)
            .processingTime(System.currentTimeMillis() - startTime)
            .build();
    }
}
