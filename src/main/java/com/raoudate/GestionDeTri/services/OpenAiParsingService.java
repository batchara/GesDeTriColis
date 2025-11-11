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
                Tu es un expert en extraction de données pour des colis au Togo.
                Ton rôle est d'analyser le texte OCR brut et d'en extraire les informations structurées.
                
                Extrait les informations suivantes (si présentes):
                - nom: Nom du destinataire ou de l'agence
                - code: Code de l'agence (numérique)
                - telephone: Numéro de téléphone (format togolais +228 XX XX XX XX)
                - email: Adresse email
                - adresse: Adresse complète formatée proprement
                - ville: Ville
                - quartier: Quartier
                - region: Région (GOLFE, MARITIME, PLATEAUX, CENTRALE, KARA, SAVANE)
                - pays: Pays (Togo par défaut)
                - notes: Informations additionnelles importantes
                - confidence: Niveau de confiance 0.0 à 1.0
                
                Réponds UNIQUEMENT avec un JSON valide, sans texte avant ou après.
                Si une information n'est pas présente, utilise null.
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
        donnees.setConfidence(0.5); // Confiance moyenne pour parsing basique

        return donnees;
    }

    private String extraireTelephone(String texte) {
        // Pattern pour numéro togolais: +228 XX XX XX XX ou (228) XX XX XX XX
        String pattern = "(?:\\+228|\\(228\\)|228)?\\s*\\d{2}\\s*\\d{2}\\s*\\d{2}\\s*\\d{2}";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(texte);
        
        if (m.find()) {
            String tel = m.group().replaceAll("[^0-9]", "");
            if (tel.length() == 8) {
                return "+228 " + tel.substring(0, 2) + " " + tel.substring(2, 4) + " " + 
                       tel.substring(4, 6) + " " + tel.substring(6, 8);
            }
        }
        return null;
    }

    private String extraireAdresse(String texte) {
        // Chercher les lignes avec des mots-clés d'adresse
        String[] lignes = texte.split("\\n");
        for (String ligne : lignes) {
            if (ligne.matches("(?i).*(rue|avenue|boulevard|quartier|route|nationale).*")) {
                return ligne.trim();
            }
        }
        return null;
    }

    private String extraireRegion(String texte) {
        String texteUpper = texte.toUpperCase();
        String[] regions = {"GOLFE", "MARITIME", "PLATEAUX", "CENTRALE", "KARA", "SAVANE"};
        
        for (String region : regions) {
            if (texteUpper.contains(region)) {
                return region;
            }
        }
        return null;
    }
}
