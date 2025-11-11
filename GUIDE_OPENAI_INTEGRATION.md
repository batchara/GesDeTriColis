# 🤖 Guide d'Intégration OpenAI - Parsing Intelligent OCR

## ✅ Ce qui a été ajouté :

### 1. **Dépendance OpenAI** (pom.xml)
```xml
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>
```

### 2. **Configuration** (application.yml)
```yaml
openai:
  api-key: ${OPENAI_API_KEY:}
  model: gpt-4o-mini
  temperature: 0.0
  max-tokens: 500
```

### 3. **Nouveau DTO** : `DonneesStructureesDTO.java`
Contient les données structurées extraites :
- `nom`, `code`, `telephone`, `email`
- `adresse`, `ville`, `quartier`, `region`, `pays`
- `notes`, `confidence`

### 4. **Nouveau Service** : `OpenAiParsingService.java`
Parse intelligemment le texte OCR brut

### 5. **Mise à jour** : `OcrService.java`
Intègre le parsing intelligent

### 6. **Mise à jour** : `ScanColisResponseDTO.java`
Ajoute le champ `donneesStructurees`

---

## 🔑 Configuration de la clé API OpenAI

### Étape 1 : Obtenir une clé API

1. **Aller sur** : https://platform.openai.com/api-keys
2. **Se connecter** (ou créer un compte)
3. **Créer une nouvelle clé** : "Create new secret key"
4. **Copier la clé** : `sk-proj-...` (vous ne pourrez la voir qu'une fois !)

### Étape 2 : Configuration

**Option A : Variable d'environnement (Recommandé)**

Dans votre terminal avant de lancer Spring Boot :
```bash
export OPENAI_API_KEY="sk-proj-votre-cle-ici"
cd /Users/batcharaoudate/Documents/Tri/TriCoBack
mvn spring-boot:run -DskipTests
```

**Option B : Directement dans application.yml** (moins sécurisé)
```yaml
openai:
  api-key: sk-proj-votre-cle-ici
```

**Option C : Fichier .env** (à ajouter au .gitignore)
```bash
# Créer un fichier .env à la racine du projet
echo "OPENAI_API_KEY=sk-proj-votre-cle-ici" > .env
```

---

## 🧪 Test dans Bruno

### Test 1 : OCR simple (sans OpenAI)

```http
POST http://localhost:8081/api/v1/scan/ocr
Authorization: Bearer VOTRE_TOKEN
Content-Type: multipart/form-data

Body:
  image: [votre image]
```

**Résultat AVANT** (texte brut) :
```json
{
  "texteExtrait": "@ Détails de l'agence\n\n@ Informations générales...",
  "adresseDetectee": "@ Détails de l'agence..."
}
```

---

### Test 2 : Scan complet (avec OpenAI)

```http
POST http://localhost:8081/api/v1/scan/colis
Authorization: Bearer VOTRE_TOKEN
Content-Type: multipart/form-data

Body:
  image: [votre image]
```

**Résultat APRÈS** (avec parsing intelligent) :
```json
{
  "texteExtrait": "@ Détails de l'agence\n\n@ Informations générales\n\n& Nom II Code\nAGBELOUVE 120...",
  "adresseDetectee": "NATIONALE N1, À COTE DE LA LIMUSCO, Agbélouvé, Togo",
  "donneesStructurees": {
    "nom": "AGBELOUVE",
    "code": "120",
    "telephone": "+228 70 59 79 07",
    "email": null,
    "adresse": "NATIONALE N1, À COTE DE LA LIMUSCO",
    "ville": "Agbélouvé",
    "quartier": null,
    "region": "MARITIME",
    "pays": "Togo",
    "notes": "Agence proche de la LIMUSCO",
    "confidence": 0.95
  },
  "geocodage": {
    "adresseComplete": "NATIONALE N1, À COTE DE LA LIMUSCO, Agbélouvé, Togo",
    "coordonnees": {
      "latitude": 6.3833,
      "longitude": 1.5167
    },
    "succes": true
  },
  "agenceLaPlusProche": {
    "code": "AGV001",
    "nom": "Agence Agbélouvé",
    "distanceKm": 0.3,
    "tempsEstime": "1 min"
  },
  "succes": true,
  "message": "Colis assigné à l'agence Agence Agbélouvé (0.3 km)"
}
```

---

## 🎯 Avantages du Parsing Intelligent

### Avant (regex basique) ❌
- Texte chaotique illisible
- Adresse mal formatée
- Impossible d'extraire téléphone/email
- Pas de structure claire

### Après (OpenAI) ✅
- **Données structurées** en JSON propre
- **Nettoyage automatique** (ex: "II" → ":")
- **Extraction intelligente** (nom, code, téléphone, région)
- **Formatage** de l'adresse pour géocodage
- **Niveau de confiance** du parsing

---

## 💰 Coûts OpenAI

### Modèle `gpt-4o-mini` (le plus économique)
- **Input** : $0.150 / 1M tokens (~$0.0001 par requête)
- **Output** : $0.600 / 1M tokens (~$0.0003 par requête)
- **Coût par scan** : ~$0.0004 (0.04 centime)

**Exemple :**
- 1000 scans/mois = $0.40
- 10,000 scans/mois = $4.00

---

## ⚙️ Fallback automatique

**Si OpenAI n'est pas disponible** (pas de clé, quota dépassé, erreur) :
- ✅ Le système utilise automatiquement le **parsing basique** (regex)
- ✅ Aucune erreur, juste moins précis
- ✅ Log : `"⚠️ Utilisation du parsing par défaut (sans IA)"`

---

## 🐛 Troubleshooting

### ❌ Erreur : "API key not configured"

**Cause** : Variable d'environnement `OPENAI_API_KEY` non définie

**Solution** :
```bash
export OPENAI_API_KEY="sk-proj-..."
```

---

### ❌ Erreur : "Rate limit exceeded"

**Cause** : Quota API dépassé

**Solutions** :
1. Attendre (reset toutes les heures)
2. Acheter plus de crédits sur OpenAI
3. Le système utilise automatiquement le fallback

---

### ❌ Parsing basique utilisé alors que OpenAI est configuré

**Vérifier les logs** :
```
✅ Réponse OpenAI reçue: {...}
✅ Parsing intelligent réussi
```

Si absent :
```
⚠️ Utilisation du parsing par défaut (sans IA)
```

**Causes possibles** :
- Clé API invalide
- Quota dépassé
- Problème réseau

---

## 📊 Monitoring

### Logs à surveiller :

**Succès :**
```
🤖 Début du parsing intelligent du texte OCR
📡 Appel à OpenAI API (modèle: gpt-4o-mini)
✅ Réponse OpenAI reçue
✅ Parsing intelligent réussi: nom=AGBELOUVE, adresse=NATIONALE N1...
```

**Fallback :**
```
❌ Erreur lors du parsing intelligent avec OpenAI
⚠️ Utilisation du parsing par défaut (sans IA)
```

---

## 🚀 Prochaines Étapes

1. **Obtenir une clé OpenAI** : https://platform.openai.com/api-keys
2. **Configurer** la variable d'environnement
3. **Redémarrer** le backend
4. **Tester** dans Bruno avec l'image d'agence
5. **Comparer** les résultats avant/après

---

## 💡 Conseils

- ✅ **Commencez** avec le modèle `gpt-4o-mini` (économique)
- ✅ **Surveillez** votre usage sur https://platform.openai.com/usage
- ✅ **Définissez** un budget limite dans OpenAI
- ✅ Le fallback garantit que le système fonctionne même sans OpenAI

---

## 📝 Résumé

L'intégration OpenAI transforme :

**Texte OCR chaotique :**
```
@ Détails de l'agence
& Nom II Code
AGBELOUVE 120
LAS Région
MARITIME
```

**En données structurées :**
```json
{
  "nom": "AGBELOUVE",
  "code": "120",
  "region": "MARITIME",
  "adresse": "NATIONALE N1, À COTE DE LA LIMUSCO",
  "telephone": "+228 70 59 79 07"
}
```

**✅ Le système est prêt ! Configurez votre clé API et testez ! 🚀**
