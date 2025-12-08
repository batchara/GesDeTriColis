package com.raoudate.GestionDeTri.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raoudate.GestionDeTri.dto.response.DonneesStructureesDTO;
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
            log.warn(" Texte OCR vide");
            return null;
        }

        // Vérifier que la clé API est configurée
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error(" Clé API OpenAI non configurée");
            return creerParsingParDefaut(texteOcr);
        }

        try {
            // Initialiser le service OpenAI
            OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(30));

            // Créer le prompt système
            String systemPrompt = """
                Tu es un expert en extraction de données de bordereaux de colis postaux au Togo.
                Ton rôle est d'analyser le texte OCR brut et d'en extraire SÉPARÉMENT les informations de l'EXPÉDITEUR et du DESTINATAIRE.
                
                 STRUCTURE TYPIQUE D'UN BORDEREAU :
                
                1. Section INFORMATIONS DU COLIS (Date, Poids, Type) → À IGNORER COMPLÈTEMENT
                2. Section EXPÉDITEUR (celui qui ENVOIE le colis)
                3. Section DESTINATAIRE (celui qui REÇOIT le colis) → C'EST LA PRIORITÉ ABSOLUE
                
                 CRITÈRE PRINCIPAL DE DISTINCTION :
                - EXPÉDITEUR : Marqué par "EXPÉDITEUR", "Expediteur", "De:", "From:", "Sender", "Envoyeur"
                - DESTINATAIRE : Marqué par "DESTINATAIRE", "Destinataire", "À:", "To:", "Receiver", "Livraison à", "Pour:", "Recipient"
                
                 RÈGLES ABSOLUES - NE JAMAIS ENFREINDRE 
                
                1. Les champs nom, prenom, telephone, adresse, ville, quartier, region, pays DOIVENT TOUJOURS venir de la section DESTINATAIRE
                2. Ces champs ne doivent JAMAIS contenir les données de l'EXPÉDITEUR
                3. Si tu vois "EXPÉDITEUR" avant "DESTINATAIRE", les premières données appartiennent à l'expéditeur, les secondes au destinataire
                4. L'expéditeur est souvent une entreprise (ex: "Service Commercial", "Société XYZ")
                5. Le destinataire est la personne/lieu qui doit RECEVOIR le colis (c'est lui qui compte pour la livraison)
                6. Si tu ne trouves PAS de section DESTINATAIRE explicite, cherche des indices ("Pour:", "Livrer à:", "À:")
                7. Ignore complètement les symboles parasites (©, @, &, =, etc.)
                
                CHAMPS À EXTRAIRE :
                
                 DESTINATAIRE (PRIORITÉ ABSOLUE - C'EST LUI QUI REÇOIT LE COLIS) :
                - nom: Nom de famille du DESTINATAIRE uniquement (ex: "ASSIROU")
                - prenom: Prénom du DESTINATAIRE uniquement (ex: "Ach")
                - telephone: Téléphone du DESTINATAIRE formaté avec +228 (ex: "+228 72 09 78 90")
                - adresse: Adresse COMPLÈTE du DESTINATAIRE (ex: "Pharmacie le progres")
                - ville: Ville du DESTINATAIRE (ex: "Lomé")
                - quartier: Quartier du DESTINATAIRE si mentionné
                - region: Région du DESTINATAIRE normalisée (GOLFE, MARITIME, PLATEAUX, CENTRALE, KARA, SAVANES)
                - pays: Toujours "Togo"
                
                 EXPÉDITEUR (SECONDAIRE - C'EST LUI QUI ENVOIE) :
                - nomExpediteur: Nom de l'EXPÉDITEUR (ex: "Service Commercial", "Société ABC")
                - telephoneExpediteur: Téléphone de l'EXPÉDITEUR avec +228
                - adresseExpediteur: Adresse de l'EXPÉDITEUR (ex: "Lomé Centre, Togo")
                
                 AUTRES (INFORMATIONS DU COLIS EN HAUT DU BORDEREAU) :
                - codeSuivi: Code de suivi du bordereau (ex: "COL-TG-2025-001234") - IMPORTANT: Utilise EXACTEMENT le code tel qu'il apparaît
                - poids: Poids du colis en kilogrammes (ex: 2.5) - Cherche "Poids estimé", "Poids", "Weight"
                - code: Code agence si présent (2-4 chiffres)
                - email: Email si présent
                - notes: Autres informations (date d'expédition, type de colis, remarques)
                - confidence: 0.0 à 1.0 (1.0 si expéditeur ET destinataire clairement identifiés)
                
                EXEMPLES :
                
                Exemple 1 (Bordereau complet):
                Texte OCR: "BORDEREAU DE COLIS\\nService de Livraison Express - Togo\\nCOL-TG-2025-001234\\n\\n@ INFORMATIONS DU COLIS\\nDate d'expédition : 12 Novembre 2025\\nPoids estimé : 2.5 kg\\nType : Standard\\n\\na EXPÉDITEUR\\nNom : Service Commercial\\nTéléphone : +228 70 12 34 56\\nAdresse : Lomé Centre, Togo\\n\\n1%: DESTINATAIRE\\nNom : ASSIROU\\nPrénom : Ach\\nTéléphone : 72097890\\nAdresse : Pharmacie le progres\\nRégion : maritime"
                
                JSON (CORRECT):
                {
                  "codeSuivi": "COL-TG-2025-001234",
                  "poids": 2.5,
                  "nom": "ASSIROU",
                  "prenom": "Ach",
                  "telephone": "+228 72 09 78 90",
                  "adresse": "Pharmacie le progres",
                  "region": "MARITIME",
                  "pays": "Togo",
                  "nomExpediteur": "Service Commercial",
                  "telephoneExpediteur": "+228 70 12 34 56",
                  "adresseExpediteur": "Lomé Centre, Togo",
                  "notes": "Poids: 2.5 kg, Type: Standard, Date: 12 Novembre 2025",
                  "confidence": 1.0
                }
                
                 JSON INCORRECT (NE JAMAIS FAIRE):
                {
                  "nom": "Service Commercial",  ← ERREUR! C'est l'expéditeur, pas le destinataire!
                  "telephone": "+228 70 12 34 56",  ← ERREUR! C'est le téléphone de l'expéditeur!
                  "adresse": "Lomé Centre, Togo",  ← ERREUR! C'est l'adresse de l'expéditeur!
                  ...
                }
                
                Exemple 2 (Structure implicite avec mots-clés):
                Texte OCR: "De: Société ABC, Sokodé\\n90123456\\nPour: Jean KOFFI\\n98765432\\nKara, Route Nationale"
                
                JSON (CORRECT):
                {
                  "nom": "KOFFI",
                  "prenom": "Jean",
                  "telephone": "+228 98 76 54 32",
                  "adresse": "Route Nationale",
                  "region": "KARA",
                  "ville": "Kara",
                  "pays": "Togo",
                  "nomExpediteur": "Société ABC",
                  "telephoneExpediteur": "+228 90 12 34 56",
                  "adresseExpediteur": "Sokodé",
                  "confidence": 0.9
                }
                
                 RÈGLE D'OR ABSOLUE 
                - Si tu vois "EXPÉDITEUR" puis "DESTINATAIRE" → ce sont DEUX personnes DIFFÉRENTES
                - Les champs nom, prenom, telephone, adresse, region = TOUJOURS le DESTINATAIRE (celui qui REÇOIT)
                - Les champs nomExpediteur, telephoneExpediteur, adresseExpediteur = l'EXPÉDITEUR (celui qui ENVOIE)
                - L'adresse à géocoder pour la livraison = UNIQUEMENT l'adresse du DESTINATAIRE
                - En cas de doute : DESTINATAIRE > EXPÉDITEUR (le destinataire est plus important)
                
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
            log.info(" Appel à OpenAI API (modèle: {})", model);
            var completion = service.createChatCompletion(request);

            String jsonResponse = completion.getChoices().get(0).getMessage().getContent();
            log.info(" Réponse OpenAI reçue: {}", jsonResponse);

            // Parser la réponse JSON
            DonneesStructureesDTO donnees = objectMapper.readValue(jsonResponse, DonneesStructureesDTO.class);
            
            // Nettoyer et valider les données
            nettoyer(donnees);
            
            log.info(" Parsing intelligent réussi: nom={}, adresse={}", 
                donnees.getNom(), donnees.getAdresse());

            // Fermer le service
            service.shutdownExecutor();

            return donnees;

        } catch (Exception e) {
            log.error(" Erreur lors du parsing intelligent avec OpenAI", e);
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
     *  IMPORTANT : On extrait UNIQUEMENT les données du DESTINATAIRE (celui qui reçoit)
     */
    private DonneesStructureesDTO creerParsingParDefaut(String texteOcr) {
        log.info(" Utilisation du parsing par défaut (sans IA)");

        DonneesStructureesDTO donnees = new DonneesStructureesDTO();
        
        // EXTRAIRE LES DONNÉES DU DESTINATAIRE
        // On cherche d'abord la section DESTINATAIRE dans le texte
        String sectionDestinataire = extraireSectionDestinataire(texteOcr);
        String texteDestinataire = (sectionDestinataire != null) ? sectionDestinataire : texteOcr;
        
        log.info(" Texte DESTINATAIRE utilisé: {}", 
                 (sectionDestinataire != null) ? "Section DESTINATAIRE isolée" : "Texte complet (pas de section DESTINATAIRE détectée)");
        
        // EXTRAIRE LES DONNÉES DE L'EXPÉDITEUR
        String sectionExpediteur = extraireSectionExpediteur(texteOcr);
        String texteExpediteur = (sectionExpediteur != null) ? sectionExpediteur : texteOcr;
        
        log.info(" Texte EXPÉDITEUR utilisé: {}", 
                 (sectionExpediteur != null) ? "Section EXPÉDITEUR isolée" : "Texte complet (pas de section EXPÉDITEUR détectée)");
        
        // ========== DESTINATAIRE ==========
        
        // Extraire le nom du DESTINATAIRE
        String nom = extraireNom(texteDestinataire);
        if (nom != null) {
            donnees.setNom(nom);
        }
        
        // Extraire le téléphone du DESTINATAIRE avec regex
        String telephone = extraireTelephone(texteDestinataire);
        if (telephone != null) {
            donnees.setTelephone(telephone);
        }

        // Extraire l'adresse du DESTINATAIRE (lignes contenant des mots-clés)
        String adresse = extraireAdresse(texteDestinataire);
        if (adresse != null) {
            donnees.setAdresse(adresse);
        }

        // Extraire la région du DESTINATAIRE
        String region = extraireRegion(texteDestinataire);
        if (region != null) {
            donnees.setRegion(region);
        }
        
        // ========== EXPÉDITEUR ==========
        
        // Extraire le nom de l'EXPÉDITEUR
        String nomExpediteur = extraireNom(texteExpediteur);
        if (nomExpediteur != null && !nomExpediteur.equals(nom)) {  // Ne pas dupliquer si c'est le même que le destinataire
            donnees.setNomExpediteur(nomExpediteur);
            log.info(" Nom expéditeur extrait (fallback): {}", nomExpediteur);
        } else {
            log.warn(" Nom expéditeur: non extrait ou identique au destinataire");
        }
        
        // ========== AUTRES CHAMPS (du texte complet) ==========
        
        // Extraire le code de suivi (chercher dans le texte COMPLET)
        String codeSuivi = extraireCodeSuivi(texteOcr);
        if (codeSuivi != null) {
            donnees.setCodeSuivi(codeSuivi);
        }
        
        // Extraire le code agence
        String code = extraireCode(texteDestinataire);
        if (code != null) {
            donnees.setCode(code);
        }
        
        // Extraire le poids du colis (de la section INFORMATIONS DU COLIS)
        Double poids = extrairePoids(texteOcr);  // On utilise le texte complet
        if (poids != null) {
            donnees.setPoids(poids);
        }

        donnees.setPays("Togo");
        
        // Confiance basée sur le nombre d'informations extraites
        int infosExtracted = 0;
        if (nom != null) infosExtracted++;
        if (code != null) infosExtracted++;
        if (telephone != null) infosExtracted++;
        if (adresse != null) infosExtracted++;
        if (region != null) infosExtracted++;
        
        double confidence = infosExtracted / 5.0;
        donnees.setConfidence(confidence);
        
        log.info(" Parsing par défaut terminé (confidence: {}): nom={}, adresse={}, region={}", 
                 confidence, nom, adresse, region);
        
        return donnees;
    }
    
    /**
     *  EXTRAIT LA SECTION DESTINATAIRE du bordereau
     * Cette méthode isole la section du destinataire pour éviter de mélanger avec l'expéditeur
     * 
     *  IMPORTANT : Gère les variations comme "1%: DESTINATAIRE", "a DESTINATAIRE", "2) DESTINATAIRE"
     */
    private String extraireSectionDestinataire(String texte) {
        String[] lignes = texte.split("\\n");
        StringBuilder sectionDestinataire = new StringBuilder();
        boolean dansDestinataire = false;
        int ligneDebut = -1;
        int ligneFin = -1;
        
        for (int i = 0; i < lignes.length; i++) {
            String ligne = lignes[i].trim();
            String ligneLower = ligne.toLowerCase();
            
            // Nettoyer les préfixes courants (1%, 2), a, b, -, *, etc.)
            String ligneNettoyee = ligne.replaceAll("^[\\d%]+[:\\-\\.\\)\\s]*", "")  // 1%, 2), 3.
                                        .replaceAll("^[a-z][:\\-\\.\\)\\s]*", "")    // a, b, c
                                        .replaceAll("^[\\*\\-\\+#]+\\s*", "")        // *, -, +, #
                                        .trim()
                                        .toLowerCase();
            
            // DETECTER DEBUT DE SECTION DESTINATAIRE (avec variations)
            if (ligneNettoyee.contains("destinataire") 
                || ligneNettoyee.startsWith("destinataire")
                || ligneNettoyee.equals("destinataire")
                || ligneLower.matches(".*\\b(destinataire|receiver|recipient|consignee)\\b.*")
                || ligneLower.contains("à:") 
                || ligneLower.contains("to:") 
                || ligneLower.contains("pour:") 
                || ligneLower.contains("livraison à")
                || ligneLower.contains("livrer à")) {
                dansDestinataire = true;
                ligneDebut = i;
                log.info(" Section DESTINATAIRE détectée à la ligne {} : '{}'", i, ligne);
                continue;
            }
            
            // DETECTER FIN DE SECTION DESTINATAIRE (ne pas inclure ces lignes)
            if (dansDestinataire) {
                // Détecter section suivante (notes, signature, etc.)
                if (ligneLower.contains("notes") 
                    || ligneLower.contains("remarques")
                    || ligneLower.contains("observations")
                    || ligneLower.contains("signature")
                    || ligneLower.contains("cachet")
                    || ligneLower.contains("date de réception")
                    || ligneLower.matches(".*-{5,}.*")  // Ligne de séparation (-----)
                    || ligneLower.matches(".*={5,}.*")) { // Ligne de séparation (=====)
                    ligneFin = i;
                    log.info(" Fin de section DESTINATAIRE détectée à la ligne {} : '{}'", i, ligne);
                    break;
                }
            }
            
            // COLLECTER LES LIGNES DE LA SECTION DESTINATAIRE
            if (dansDestinataire && !ligne.isEmpty()) {
                sectionDestinataire.append(ligne).append("\n");
            }
        }
        
        // Si aucune fin détectée, prendre jusqu'à la fin du document
        if (dansDestinataire && ligneFin == -1) {
            ligneFin = lignes.length;
            log.info(" Section DESTINATAIRE va jusqu'à la fin du document (ligne {})", ligneFin);
        }
        
        String resultat = sectionDestinataire.toString().trim();
        
        if (!resultat.isEmpty()) {
            log.info(" Section DESTINATAIRE extraite avec succès ({} lignes) :\n{}", 
                     ligneFin - ligneDebut, 
                     resultat.length() > 200 ? resultat.substring(0, 200) + "..." : resultat);
            return resultat;
        }
        
        log.warn(" Aucune section DESTINATAIRE explicite trouvée, utilisation du texte complet");
        return null;
    }
    
    /**
     *  EXTRAIT LA SECTION EXPÉDITEUR du bordereau
     * Cette méthode isole la section de l'expéditeur pour éviter de mélanger avec le destinataire
     */
    private String extraireSectionExpediteur(String texte) {
        String[] lignes = texte.split("\\n");
        StringBuilder sectionExpediteur = new StringBuilder();
        boolean dansExpediteur = false;
        int ligneDebut = -1;
        int ligneFin = -1;
        
        for (int i = 0; i < lignes.length; i++) {
            String ligne = lignes[i].trim();
            String ligneLower = ligne.toLowerCase();
            
            // Nettoyer les préfixes courants
            String ligneNettoyee = ligne.replaceAll("^[\\d%]+[:\\-\\.\\)\\s]*", "")
                                        .replaceAll("^[a-z][:\\-\\.\\)\\s]*", "")
                                        .replaceAll("^[\\*\\-\\+#]+\\s*", "")
                                        .trim()
                                        .toLowerCase();
            
            // DETECTER DEBUT DE SECTION EXPÉDITEUR
            if (ligneNettoyee.contains("expéditeur") 
                || ligneNettoyee.contains("expediteur")
                || ligneNettoyee.startsWith("expéditeur")
                || ligneNettoyee.startsWith("expediteur")
                || ligneLower.matches(".*\\b(expéditeur|expediteur|sender|from)\\b.*")
                || ligneLower.contains("de:") 
                || ligneLower.contains("from:") 
                || ligneLower.contains("envoyeur")) {
                dansExpediteur = true;
                ligneDebut = i;
                log.info(" Section EXPÉDITEUR détectée à la ligne {} : '{}'", i, ligne);
                continue;
            }
            
            // DETECTER FIN DE SECTION EXPÉDITEUR
            if (dansExpediteur) {
                // Si on rencontre la section DESTINATAIRE, on arrête
                if (ligneLower.contains("destinataire") 
                    || ligneLower.contains("à:") 
                    || ligneLower.contains("to:") 
                    || ligneLower.contains("pour:")
                    || ligneLower.contains("livraison")
                    || ligneLower.contains("receiver")) {
                    ligneFin = i;
                    log.info(" Fin de section EXPÉDITEUR détectée à la ligne {} (début DESTINATAIRE): '{}'", i, ligne);
                    break;
                }
            }
            
            // COLLECTER LES LIGNES DE LA SECTION EXPÉDITEUR
            if (dansExpediteur && !ligne.isEmpty()) {
                sectionExpediteur.append(ligne).append("\n");
            }
        }
        
        // Si aucune fin détectée, prendre jusqu'à la fin du document ou jusqu'au destinataire
        if (dansExpediteur && ligneFin == -1) {
            ligneFin = lignes.length;
            log.info(" Section EXPÉDITEUR va jusqu'à la ligne {}", ligneFin);
        }
        
        String resultat = sectionExpediteur.toString().trim();
        
        if (!resultat.isEmpty()) {
            log.info(" Section EXPÉDITEUR extraite avec succès ({} lignes) :\n{}", 
                     ligneFin - ligneDebut, 
                     resultat.length() > 200 ? resultat.substring(0, 200) + "..." : resultat);
            return resultat;
        }
        
        log.warn(" Aucune section EXPÉDITEUR explicite trouvée");
        return null;
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
     * Extrait le code de suivi du bordereau (ex: COL-TG-2025-001234)
     * Format typique: XXX-XX-YYYY-NNNNNN
     */
    private String extraireCodeSuivi(String texte) {
        String[] lignes = texte.split("\\n");
        
        // Pattern pour code de suivi: COL-TG-2025-001234 ou similaire
        // Format: 2-5 lettres, tiret, 2-5 lettres/chiffres, tiret, 4 chiffres (année), tiret, 4-8 chiffres
        String patternCodeSuivi = "\\b([A-Z]{2,5}-[A-Z]{2,5}-\\d{4}-\\d{4,8})\\b";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(patternCodeSuivi);
        
        for (String ligne : lignes) {
            java.util.regex.Matcher m = p.matcher(ligne);
            if (m.find()) {
                String code = m.group(1);
                log.info(" Code de suivi trouvé: {}", code);
                return code;
            }
        }
        
        // Pattern alternatif: chercher après "Code de suivi:", "N°:", "Référence:", etc.
        for (String ligne : lignes) {
            String ligneLower = ligne.toLowerCase();
            
            if (ligneLower.matches(".*(code de suivi|n°|référence|reference|tracking|numéro)\\s*:.*")) {
                String[] parts = ligne.split(":", 2);
                if (parts.length > 1) {
                    String code = parts[1].trim();
                    // Vérifier que c'est bien un code (pas juste des espaces)
                    if (code.length() > 5 && code.matches(".*[A-Z0-9-]+.*")) {
                        log.info(" Code de suivi trouvé (label explicite): {}", code);
                        return code;
                    }
                }
            }
        }
        
        log.warn(" Aucun code de suivi trouvé dans le texte OCR");
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

    /**
     *  EXTRACTION INTELLIGENTE D'ADRESSE - Stratégie multi-niveaux
     * 
     * Cette méthode utilise 4 stratégies en cascade pour extraire l'adresse du destinataire :
     * 1. PRIORITE MAX : Chercher "Adresse :" dans section DESTINATAIRE
     * 2. PRIORITE HAUTE : Patterns typiques (Pharmacie, Gare, Marché, Route, etc.)
     * 3. PRIORITE MOYENNE : Noms de quartiers/villes connus du Togo
     * 4. FALLBACK : Analyse contextuelle (après "Nom/Téléphone" dans section DESTINATAIRE)
     */
    private String extraireAdresse(String texte) {
        String[] lignes = texte.split("\\n");
        
        // STRATEGIE 1 : Chercher explicitement "Adresse :" (PRIORITE MAXIMALE)
        String adresseExplicite = chercherAdresseExplicite(lignes);
        if (adresseExplicite != null) {
            log.info(" Adresse extraite (stratégie 1 - explicite): {}", adresseExplicite);
            return adresseExplicite;
        }
        
        // STRATEGIE 2 : Patterns typiques d'adresses (Pharmacie, Gare, Marché, etc.)
        String adressePattern = chercherAdresseParPattern(lignes);
        if (adressePattern != null) {
            log.info(" Adresse extraite (stratégie 2 - pattern): {}", adressePattern);
            return adressePattern;
        }
        
        // STRATEGIE 3 : Quartiers/Villes connus du Togo
        String adresseVille = chercherAdresseParVille(lignes);
        if (adresseVille != null) {
            log.info(" Adresse extraite (stratégie 3 - ville): {}", adresseVille);
            return adresseVille;
        }
        
        // STRATEGIE 4 : Analyse contextuelle (section DESTINATAIRE)
        String adresseContextuelle = chercherAdresseContextuelle(lignes);
        if (adresseContextuelle != null) {
            log.info(" Adresse extraite (stratégie 4 - contextuelle): {}", adresseContextuelle);
            return adresseContextuelle;
        }
        
        log.warn(" Aucune adresse trouvée avec les 4 stratégies");
        return null;
    }
    
    /**
     * STRATEGIE 1 : Chercher "Adresse :" explicitement
     */
    private String chercherAdresseExplicite(String[] lignes) {
        for (int i = 0; i < lignes.length; i++) {
            String ligne = lignes[i].trim();
            String ligneLower = ligne.toLowerCase();
            
            // Chercher "adresse", "localisation", "address", "lieu"
            if (ligneLower.matches(".*(adresse|localisation|address|lieu)\\s*:.*")) {
                
                // Cas 1 : Adresse sur la même ligne après ":"
                if (ligne.contains(":")) {
                    String[] parts = ligne.split(":", 2);
                    if (parts.length > 1) {
                        String adresse = parts[1].trim();
                        if (estAdresseValide(adresse)) {
                            return adresse;
                        }
                    }
                }
                
                // Cas 2 : Adresse sur les lignes suivantes
                for (int j = i + 1; j < Math.min(i + 5, lignes.length); j++) {
                    String ligneSuivante = lignes[j].trim();
                    
                    if (ligneSuivante.isEmpty()) continue;
                    if (!estAdresseValide(ligneSuivante)) continue;
                    
                    return ligneSuivante;
                }
            }
        }
        return null;
    }
    
    /**
     * STRATEGIE 2 : Patterns typiques d'adresses au Togo
     */
    private String chercherAdresseParPattern(String[] lignes) {
        // Patterns d'établissements et lieux
        String[] patterns = {
            "pharmacie", "gare", "marché", "marche", "maison", "immeuble",
            "route nationale", "route", "boulevard", "avenue", "rue",
            "près", "pres", "côté", "cote", "face", "derrière", "devant",
            "magasin", "boutique", "restaurant", "hôtel", "hotel",
            "église", "eglise", "mosquée", "mosquee", "école", "ecole",
            "hôpital", "hopital", "centre", "marche", "station",
            "carrefour", "rond-point", "pont", "stade", "aéroport"
        };
        
        for (String ligne : lignes) {
            String ligneLower = ligne.trim().toLowerCase();
            
            // Vérifier si contient un pattern
            for (String pattern : patterns) {
                if (ligneLower.contains(pattern) && estAdresseValide(ligne.trim())) {
                    return ligne.trim();
                }
            }
        }
        return null;
    }
    
    /**
     * STRATEGIE 3 : Noms de villes et quartiers connus du Togo
     */
    private String chercherAdresseParVille(String[] lignes) {
        // Villes principales du Togo
        String[] villes = {
            "lomé", "lome", "kara", "sokodé", "sokode", "atakpamé", "atakpame",
            "kpalimé", "kpalime", "tsévié", "tsevie", "aného", "aneho",
            "bassar", "dapaong", "niamtougou", "bafilo", "notsé", "notse",
            "vogan", "tabligbo", "tchamba", "sotouboua"
        };
        
        // Quartiers de Lomé et environs
        String[] quartiers = {
            "adidogome", "agoè", "agoe", "nyékonakpoè", "nyekonakpoe", "bè", "be",
            "tokoin", "amoutivé", "amoutive", "hédzranawoé", "hedzranawoe",
            "démakpoè", "demakpoe", "kagomé", "kagome", "djidjolé", "djidjole",
            "kégué", "kegue", "anfamé", "anfame", "amadahomé", "amadahome",
            "agbalépédogan", "agbalepedogan", "légbassito", "legbassito"
        };
        
        for (String ligne : lignes) {
            String ligneLower = ligne.trim().toLowerCase();
            
            // Vérifier villes
            for (String ville : villes) {
                if (ligneLower.contains(ville) && estAdresseValide(ligne.trim())) {
                    return ligne.trim();
                }
            }
            
            // Vérifier quartiers
            for (String quartier : quartiers) {
                if (ligneLower.contains(quartier) && estAdresseValide(ligne.trim())) {
                    return ligne.trim();
                }
            }
        }
        return null;
    }
    
    /**
     * STRATEGIE 4 : Analyse contextuelle - Section DESTINATAIRE
     * Cherche l'adresse après avoir trouvé "Nom" et "Téléphone" du destinataire
     */
    private String chercherAdresseContextuelle(String[] lignes) {
        boolean dansDestinataire = false;
        boolean nomTrouve = false;
        boolean telTrouve = false;
        
        for (int i = 0; i < lignes.length; i++) {
            String ligne = lignes[i].trim();
            String ligneLower = ligne.toLowerCase();
            
            // Détecter section DESTINATAIRE
            if (ligneLower.contains("destinataire") || ligneLower.contains("livraison")) {
                dansDestinataire = true;
                continue;
            }
            
            // Détecter fin de section
            if (ligneLower.contains("expéditeur") || ligneLower.contains("expediteur") 
                || ligneLower.contains("colis") || ligneLower.contains("informations")) {
                dansDestinataire = false;
            }
            
            if (dansDestinataire) {
                // Marquer Nom trouvé
                if (ligneLower.contains("nom") && ligneLower.contains(":")) {
                    nomTrouve = true;
                }
                
                // Marquer Téléphone trouvé
                if (ligneLower.contains("téléphone") || ligneLower.contains("telephone") 
                    || ligneLower.contains("tel") || ligne.matches(".*\\d{8}.*")) {
                    telTrouve = true;
                }
                
                // Si Nom ET Téléphone trouvés, la prochaine ligne non-label est l'adresse
                if (nomTrouve && telTrouve) {
                    for (int j = i + 1; j < Math.min(i + 5, lignes.length); j++) {
                        String ligneSuivante = lignes[j].trim();
                        String ligneSuivanteLower = ligneSuivante.toLowerCase();
                        
                        // Ignorer les labels
                        if (ligneSuivanteLower.matches(".*(adresse|région|region|pays|code)\\s*:.*")) {
                            continue;
                        }
                        
                        if (estAdresseValide(ligneSuivante)) {
                            return ligneSuivante;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Vérifie si une chaîne est une adresse valide
     */
    private boolean estAdresseValide(String texte) {
        if (texte == null || texte.trim().isEmpty()) return false;
        
        String texteLower = texte.toLowerCase();
        
        // Longueur minimale
        if (texte.length() < 4) return false;
        
        //  Rejeter symboles parasites
        if (texte.matches(".*[©@&={}\\[\\]].*")) return false;
        
        //  Rejeter coordonnées GPS pures
        if (texte.matches("^[0-9.\\s]+$")) return false;
        
        //  Rejeter labels
        if (texteLower.matches("^(nom|prénom|prenom|téléphone|telephone|email|region|pays|code)\\s*:?$")) return false;
        
        //  Rejeter informations de colis
        String[] motsInterdits = {
            "date d'expédition", "date expedition", "poids estimé", "poids estime",
            "type :", "standard", "express", "novembre", "décembre", "janvier",
            " kg", "grammes", "service de livraison", "bordereau",
            "informations du colis", "expéditeur", "code de suivi"
        };
        
        for (String interdit : motsInterdits) {
            if (texteLower.contains(interdit)) {
                return false;
            }
        }
        
        //  Rejeter si c'est UNIQUEMENT un nom de région
        String[] regionsSeules = {"golfe", "maritime", "plateaux", "centrale", "kara", "savanes", "togo"};
        for (String region : regionsSeules) {
            if (texteLower.trim().equals(region)) {
                return false;
            }
        }
        
        // Accepter si contient au moins quelques lettres
        return texte.matches(".*[a-zA-ZÀ-ÿ]{3,}.*");
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
    
    /**
     * Extrait le poids du colis depuis la section INFORMATIONS DU COLIS
     * Cherche des patterns comme:
     * - "Poids estimé : 2.5 kg"
     * - "Poids: 3kg"
     * - "Weight: 1.5 kg"
     * - "2.5 kg"
     */
    private Double extrairePoids(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return null;
        }
        
        String texteLower = texte.toLowerCase();
        
        // Pattern 1: "Poids estimé : 2.5 kg" ou "Poids : 3 kg"
        java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile(
            "(poids\\s*(?:estimé|estime)?\\s*:?\\s*)(\\d+\\.?\\d*)\\s*(kg|kilogrammes?|kilos?)",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher1 = pattern1.matcher(texte);
        if (matcher1.find()) {
            try {
                double poids = Double.parseDouble(matcher1.group(2));
                log.info(" Poids extrait (pattern 1): {} kg", poids);
                return poids;
            } catch (NumberFormatException e) {
                log.warn(" Erreur de parsing du poids: {}", matcher1.group(2));
            }
        }
        
        // Pattern 2: "Weight: 2.5 kg"
        java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile(
            "(weight\\s*:?\\s*)(\\d+\\.?\\d*)\\s*(kg|kilogrammes?|kilos?)",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher2 = pattern2.matcher(texte);
        if (matcher2.find()) {
            try {
                double poids = Double.parseDouble(matcher2.group(2));
                log.info(" Poids extrait (pattern 2): {} kg", poids);
                return poids;
            } catch (NumberFormatException e) {
                log.warn(" Erreur de parsing du poids: {}", matcher2.group(2));
            }
        }
        
        // Pattern 3: Juste un nombre suivi de "kg" (ex: "2.5 kg")
        // Mais uniquement dans la section INFORMATIONS DU COLIS
        if (texteLower.contains("informations du colis")) {
            java.util.regex.Pattern pattern3 = java.util.regex.Pattern.compile(
                "\\b(\\d+\\.?\\d*)\\s*(kg|kilogrammes?|kilos?)\\b",
                java.util.regex.Pattern.CASE_INSENSITIVE
            );
            java.util.regex.Matcher matcher3 = pattern3.matcher(texte);
            if (matcher3.find()) {
                try {
                    double poids = Double.parseDouble(matcher3.group(1));
                    log.info(" Poids extrait (pattern 3): {} kg", poids);
                    return poids;
                } catch (NumberFormatException e) {
                    log.warn(" Erreur de parsing du poids: {}", matcher3.group(1));
                }
            }
        }
        
        log.warn(" Aucun poids trouvé dans le texte OCR");
        return null;
    }
}
