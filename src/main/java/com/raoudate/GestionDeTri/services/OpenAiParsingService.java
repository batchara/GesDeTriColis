package com.raoudate.GestionDeTri.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raoudate.GestionDeTri.Dto.DonneesStructureesDTO;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Service pour l'analyse intelligente du texte OCR via OpenAI
 */
@Service
@Slf4j
public class OpenAiParsingService {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.temperature:0.0}")
    private Double temperature;

    @Value("${openai.max-tokens:500}")
    private Integer maxTokens;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Analyse et structure le texte OCR brut en données exploitables
     */
    public DonneesStructureesDTO parserTexteOcr(String texteOcr) {
        log.info("🤖 Début du parsing intelligent du texte OCR");

        if (texteOcr == null || texteOcr.trim().isEmpty()) {
            log.warn("⚠️ Texte OCR vide");
            return null;
        }

        // Vérifier que la clé API est configurée
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("❌ Clé API OpenAI non configurée");
            return creerParsingParDefaut(texteOcr);
        }

        try {
            // Initialiser le service OpenAI
            OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(30));

            // Créer le prompt système
            String systemPrompt = """
                Tu es un expert en extraction de données de bordereaux de colis postaux au Togo.
                Ton rôle est d'analyser le texte OCR brut (qui peut provenir d'un BORDEREAU MANUSCRIT ou IMPRIMÉ) et d'en extraire les informations structurées.
                
                IMPORTANT - RÈGLES D'EXTRACTION:
                1. Le texte peut être MANUSCRIT (écriture à la main) ou IMPRIMÉ
                2. L'écriture peut être en MINUSCULES, MAJUSCULES, ou Mixte
                3. Ignore TOUS les symboles, icônes et caractères spéciaux (©, @, &, =, D, Q, eo, IL, LAS, etc.)
                4. Cherche les VRAIES VALEURS après les labels/étiquettes
                5. Sois TRÈS flexible dans la reconnaissance des patterns
                
                EXTRACTION DES CHAMPS:
                
                NOM DE L'AGENCE / DESTINATAIRE:
                - Cherche après les mots: "Nom", "Agence", "Destinataire", "Name"
                - Peut être en MAJUSCULES (ex: LOME AGOE-ASSIYEYE) ou minuscules (ex: Lomé Agoe)
                - Peut contenir des tirets, espaces, accents
                - Ignore les numéros qui suivent (c'est le code)
                
                CODE:
                - Nombre de 2 à 4 chiffres (ex: 619, 120, 85)
                - Souvent juste après le nom
                - Peut être sur la même ligne ou ligne suivante
                - Ignore les numéros de téléphone (8 chiffres)
                
                RÉGION:
                - Valeurs possibles: GOLFE, MARITIME, PLATEAUX, CENTRALE, KARA, SAVANES
                - Variations OCR courantes: "GOLF" → "GOLFE", "SAVANE" → "SAVANES"
                - Peut être après "Région", "Region", ou seul sur une ligne
                
                ADRESSE:
                - Cherche après: "Adresse", "Adresse complète", "Localisation", "Address"
                - Peut contenir: rue, avenue, quartier, marché, route, nationale, côté, près de, etc.
                - TOUJOURS en minuscules ou MAJUSCULES
                - Ignore les symboles avant (D, Q, eo, etc.)
                
                TÉLÉPHONE:
                - Format Togo: +228 XX XX XX XX ou (228) XX XX XX XX
                - Peut avoir ou non l'indicatif +228
                - 8 chiffres au total (sans l'indicatif)
                - Cherche après "Téléphone", "Tel", "Phone", ou symbole "="
                
                EMAIL:
                - Format standard email
                - Cherche après "Email", "E-mail", "Mail"
                
                EXEMPLES RÉELS:
                
                Exemple 1 (Majuscules):
                Texte OCR: "Nom IL Code\\nLOME AGOE-ASSIYEYE 619\\nRégion\\nGOLF\\nAdresse\\nA COTE DU MARCHE"
                → nom: "LOME AGOE-ASSIYEYE", code: "619", region: "GOLFE", adresse: "A COTE DU MARCHE"
                
                Exemple 2 (Minuscules manuscrites):
                Texte OCR: "destinataire: lomé agoe\\ncode: 619\\nrégion: golfe\\nadresse: côté marché agoe"
                → nom: "lomé agoe", code: "619", region: "GOLFE", adresse: "côté marché agoe"
                
                Exemple 3 (Mixte):
                Texte OCR: "Agence: Lomé Agoe-Assiyeye\\n619\\nGOLFE\\nRue du Marché, Agoe"
                → nom: "Lomé Agoe-Assiyeye", code: "619", region: "GOLFE", adresse: "Rue du Marché, Agoe"
                
                CHAMPS À EXTRAIRE:
                - nom: Nom de l'agence ou du destinataire (préserver la casse originale)
                - code: Code numérique de l'agence (2-4 chiffres)
                - telephone: Numéro formaté avec +228
                - email: Adresse email si présente
                - adresse: Adresse complète nettoyée (sans symboles)
                - ville: Ville extraite du nom ou de l'adresse
                - quartier: Quartier si mentionné
                - region: Région normalisée en MAJUSCULES (GOLFE, MARITIME, etc.)
                - pays: Toujours "Togo"
                - notes: Informations additionnelles pertinentes
                - confidence: 0.0 à 1.0 (0.9+ si nom, code, region et adresse trouvés)
                
                QUALITÉ:
                - Confiance 1.0 si nom + code + région + adresse trouvés
                - Confiance 0.8 si nom + adresse trouvés
                - Confiance 0.5 si seulement adresse ou nom trouvé
                - Confiance 0.3 si très peu d'infos
                
                Réponds UNIQUEMENT avec un JSON valide, sans markdown (```json), sans texte avant ou après.
                Si une information n'est pas trouvée, utilise null.
                """;

            String userPrompt = "Analyse ce texte OCR et extrait les données structurées:\n\n" + texteOcr;

            // Créer les messages
            List<ChatMessage> messages = Arrays.asList(
                new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
                new ChatMessage(ChatMessageRole.USER.value(), userPrompt)
            );

            // Créer la requête
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

            // Appeler l'API
            log.info("📡 Appel à OpenAI API (modèle: {})", model);
            var completion = service.createChatCompletion(request);

            String jsonResponse = completion.getChoices().get(0).getMessage().getContent();
            log.info("✅ Réponse OpenAI reçue: {}", jsonResponse);

            // Parser la réponse JSON
            DonneesStructureesDTO donnees = objectMapper.readValue(jsonResponse, DonneesStructureesDTO.class);
            
            // Nettoyer et valider les données
            nettoyer(donnees);
            
            log.info("✅ Parsing intelligent réussi: nom={}, adresse={}", 
                donnees.getNom(), donnees.getAdresse());

            // Fermer le service
            service.shutdownExecutor();

            return donnees;

        } catch (Exception e) {
            log.error("❌ Erreur lors du parsing intelligent avec OpenAI", e);
            // Fallback sur le parsing par défaut
            return creerParsingParDefaut(texteOcr);
        }
    }

    /**
     * Nettoie et valide les données extraites
     */
    private void nettoyer(DonneesStructureesDTO donnees) {
        if (donnees == null) return;

        // Nettoyer le téléphone
        if (donnees.getTelephone() != null) {
            String tel = donnees.getTelephone()
                .replaceAll("[^0-9+]", "")
                .trim();
            
            // Ajouter +228 si manquant
            if (!tel.startsWith("+228") && tel.length() == 8) {
                tel = "+228" + tel;
            }
            donnees.setTelephone(tel);
        }

        // S'assurer que le pays est "Togo"
        if (donnees.getPays() == null || donnees.getPays().trim().isEmpty()) {
            donnees.setPays("Togo");
        }

        // Nettoyer les espaces
        if (donnees.getNom() != null) donnees.setNom(donnees.getNom().trim());
        if (donnees.getAdresse() != null) donnees.setAdresse(donnees.getAdresse().trim());
        if (donnees.getVille() != null) donnees.setVille(donnees.getVille().trim());
    }

    /**
     * Crée un parsing basique si OpenAI n'est pas disponible
     */
    private DonneesStructureesDTO creerParsingParDefaut(String texteOcr) {
        log.info("⚠️ Utilisation du parsing par défaut (sans IA)");

        DonneesStructureesDTO donnees = new DonneesStructureesDTO();
        
        // Extraire le nom de l'agence
        String nom = extraireNom(texteOcr);
        if (nom != null) {
            donnees.setNom(nom);
        }
        
        // Extraire le code
        String code = extraireCode(texteOcr);
        if (code != null) {
            donnees.setCode(code);
        }
        
        // Extraire le téléphone avec regex
        String telephone = extraireTelephone(texteOcr);
        if (telephone != null) {
            donnees.setTelephone(telephone);
        }

        // Extraire l'adresse (lignes contenant des mots-clés)
        String adresse = extraireAdresse(texteOcr);
        if (adresse != null) {
            donnees.setAdresse(adresse);
        }

        // Extraire la région
        String region = extraireRegion(texteOcr);
        if (region != null) {
            donnees.setRegion(region);
        }

        donnees.setPays("Togo");
        
        // Confiance basée sur le nombre d'informations extraites
        int infosExtracted = 0;
        if (nom != null) infosExtracted++;
        if (code != null) infosExtracted++;
        if (telephone != null) infosExtracted++;
        if (adresse != null) infosExtracted++;
        if (region != null) infosExtracted++;
        
        donnees.setConfidence(infosExtracted / 5.0); // 0.0 à 1.0

        return donnees;
    }
    
    /**
     * Extrait le nom de l'agence (flexible, indépendant de la casse)
     */
    private String extraireNom(String texte) {
        String[] lignes = texte.split("\\n");
        
        for (int i = 0; i < lignes.length; i++) {
            String ligne = lignes[i].trim();
            String ligneLower = ligne.toLowerCase();
            
            // Chercher après les labels "Nom", "Agence", "Destinataire"
            if (ligneLower.matches(".*(nom|agence|destinataire|name).*") && i + 1 < lignes.length) {
                String ligneSuivante = lignes[i + 1].trim();
                
                // Si c'est sur la même ligne après ":", extraire
                if (ligne.contains(":")) {
                    String[] parts = ligne.split(":", 2);
                    if (parts.length > 1) {
                        String nom = parts[1].trim();
                        // Enlever le code s'il est collé
                        nom = nom.replaceAll("\\s+\\d{2,4}$", "").trim();
                        if (nom.length() > 2 && !nom.matches(".*[©@&=].*")) {
                            return nom;
                        }
                    }
                }
                
                // Si sur la ligne suivante
                if (!ligneSuivante.matches(".*[©@&=DQeo].*") && ligneSuivante.length() > 2) {
                    // Enlever le code à la fin
                    String nom = ligneSuivante.replaceAll("\\s+\\d{2,4}$", "").trim();
                    if (nom.length() > 2) {
                        return nom;
                    }
                }
            }
            
            // Pattern: Nom suivi de chiffres (ex: "LOME AGOE 619" ou "lomé agoe 619")
            if (ligne.matches("^[A-Za-zÀ-ÿ][A-Za-zÀ-ÿ\\s-]{3,}\\s+\\d{2,4}$")) {
                String nom = ligne.replaceAll("\\s+\\d{2,4}$", "").trim();
                return nom;
            }
        }
        
        return null;
    }
    
    /**
     * Extrait le code de l'agence (2-4 chiffres)
     */
    private String extraireCode(String texte) {
        String[] lignes = texte.split("\\n");
        
        for (String ligne : lignes) {
            String ligneLower = ligne.toLowerCase();
            
            // Chercher après "code:"
            if (ligneLower.contains("code") && ligne.contains(":")) {
                String[] parts = ligne.split(":", 2);
                if (parts.length > 1) {
                    String code = parts[1].trim().replaceAll("\\D", "");
                    if (code.length() >= 2 && code.length() <= 4) {
                        return code;
                    }
                }
            }
        }
        
        // Pattern pour code (2-4 chiffres isolés, pas dans un téléphone)
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\b(\\d{2,4})\\b");
        java.util.regex.Matcher m = p.matcher(texte);
        
        while (m.find()) {
            String code = m.group(1);
            // Ignorer les années, parties de téléphone
            if (code.length() >= 2 && code.length() <= 4 
                && !code.startsWith("20") 
                && !code.startsWith("22")) {
                return code;
            }
        }
        
        return null;
    }

    private String extraireTelephone(String texte) {
        // Pattern pour numéro togolais: +228 XX XX XX XX ou (228) XX XX XX XX
        // Accepte aussi des formats avec = avant le numéro
        String pattern = "(?:=\\s*)?(?:\\+228|\\(228\\)|228)?\\s*\\d{2}\\s*\\d{2}\\s*\\d{2}\\s*\\d{2}";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(texte);
        
        if (m.find()) {
            String tel = m.group().replaceAll("[^0-9]", "");
            if (tel.length() == 8) {
                return "+228 " + tel.substring(0, 2) + " " + tel.substring(2, 4) + " " + 
                       tel.substring(4, 6) + " " + tel.substring(6, 8);
            } else if (tel.length() == 11 && tel.startsWith("228")) {
                // Cas où le code pays est inclus (228XXXXXXXX)
                String numLocal = tel.substring(3);
                return "+228 " + numLocal.substring(0, 2) + " " + numLocal.substring(2, 4) + " " + 
                       numLocal.substring(4, 6) + " " + numLocal.substring(6, 8);
            }
        }
        return null;
    }

    private String extraireAdresse(String texte) {
        String[] lignes = texte.split("\\n");
        
        for (int i = 0; i < lignes.length; i++) {
            String ligne = lignes[i].trim();
            String ligneLower = ligne.toLowerCase();
            
            // Chercher après "adresse:", "adresse complète:", "o adresse", etc.
            if (ligneLower.matches(".*(adresse|localisation|address).*")) {
                
                // Si l'adresse est sur la même ligne après ":"
                if (ligne.contains(":")) {
                    String[] parts = ligne.split(":", 2);
                    if (parts.length > 1) {
                        String adresse = parts[1].trim();
                        // Accepter les adresses avec apostrophes et accents
                        if (!adresse.isEmpty() && !adresse.matches(".*[©@&=DQeoIL{}].*") && adresse.length() > 3) {
                            return adresse;
                        }
                    }
                }
                
                // Si l'adresse est sur les lignes suivantes (sauter les lignes vides)
                for (int j = i + 1; j < Math.min(i + 4, lignes.length); j++) {
                    String ligneSuivante = lignes[j].trim();
                    
                    // Ignorer les lignes vides
                    if (ligneSuivante.isEmpty()) continue;
                    
                    // Ignorer les lignes avec symboles parasites ou coordonnées GPS
                    if (ligneSuivante.matches(".*[©@&=DQeoIL{}].*")) continue;
                    if (ligneSuivante.matches(".*[Ll]atitude.*|.*[Ll]ongitude.*")) continue;
                    if (ligneSuivante.matches("^[0-9.\\s]+$")) continue; // Ligne de coordonnées
                    
                    // Si ligne valide et assez longue
                    if (ligneSuivante.length() > 3) {
                        return ligneSuivante;
                    }
                }
            }
            
            // Patterns typiques d'adresses au Togo (GARE, MARCHE, MAISON, etc.)
            if (ligneLower.matches(".*(gare|marché|marche|maison|route nationale|boulevard|avenue|près|pres|cote|côté).*") 
                && !ligne.matches(".*[©@&=QIL{}].*")
                && ligne.length() >= 5) {
                return ligne.trim();
            }
        }
        
        return null;
    }

    private String extraireRegion(String texte) {
        String texteUpper = texte.toUpperCase();
        
        // Régions officielles du Togo
        String[] regions = {"GOLFE", "MARITIME", "PLATEAUX", "CENTRALE", "KARA", "SAVANES"};
        
        // D'abord chercher les régions exactes
        for (String region : regions) {
            if (texteUpper.contains(region)) {
                return region;
            }
        }
        
        // Ensuite chercher les erreurs OCR courantes
        if (texteUpper.contains("GOLF")) {
            return "GOLFE";
        }
        if (texteUpper.contains("SAVANE")) {
            return "SAVANES";
        }
        if (texteUpper.contains("PLATEAU")) {
            return "PLATEAUX";
        }
        
        return null;
    }
}
