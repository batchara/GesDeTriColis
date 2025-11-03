# 📋 Système de Logs d'Audit - TriCO

## Vue d'ensemble

Le système de logs d'audit TriCO enregistre toutes les actions importantes effectuées dans l'application dans un fichier texte (`logs/audit.log`). Ce système offre une traçabilité complète des opérations.

## 🎯 Types de logs enregistrés

### 🔐 Authentification & Sécurité
- ✅ Connexions réussies
- ❌ Tentatives de connexion échouées
- 🔒 Tentatives d'accès à un compte bloqué
- 📧 Tentatives d'accès à un compte non activé
- 🚪 Déconnexions

### 👥 Gestion des Utilisateurs
- ➕ Création de nouveaux utilisateurs
- ✏️ Modification des informations utilisateur
- 🗑️ Suppression/désactivation de comptes
- 🎭 Attribution/révocation de rôles
- 🔑 Réinitialisation de mots de passe

### 📝 Opérations CRUD
Pour chaque entité critique (Colis, Agences, etc.) :
- **CREATE** : Création d'entité
- **UPDATE** : Modification d'entité
- **DELETE** : Suppression d'entité

### ⚙️ Actions Système
- 🗂️ Effacement des logs
- ⚠️ Erreurs applicatives
- 🔧 Modifications de configuration

## 📊 Structure d'un Log

```json
{
  "timestamp": "2025-11-03T10:30:45.123",
  "userId": "12345",
  "username": "admin@example.com",
  "userRole": "ROLE_ADMIN",
  "action": "UPDATE_USER",
  "entityType": "User",
  "entityId": "67890",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "oldValue": "...",
  "newValue": "...",
  "status": "SUCCESS",
  "message": "User account deactivated"
}
```

## 🛠️ Architecture Technique

### Backend (Spring Boot)

#### 1. **AuditLog.java**
Modèle de données représentant un log d'audit

#### 2. **AuditLogService.java**
Service principal pour :
- Enregistrer des logs
- Lire les logs depuis le fichier
- Filtrer les logs
- Effacer les logs

#### 3. **AuditLogController.java**
API REST exposant :
- `GET /api/v1/audit/logs` - Récupérer tous les logs
- `GET /api/v1/audit/logs/filter` - Récupérer les logs filtrés
- `DELETE /api/v1/audit/logs` - Effacer tous les logs (admin only)

#### 4. **AuditAspect.java**
Aspect AOP pour logger automatiquement les méthodes annotées avec `@Auditable`

#### 5. **@Auditable**
Annotation personnalisée pour marquer les méthodes à auditer

### Frontend (Angular)

#### 1. **audit-log.service.ts**
Service Angular pour communiquer avec l'API backend

#### 2. **Section Journal d'Activités**
Interface dans `home-admin.component.html` avec :
- 📊 Statistiques rapides (Total, Succès, Échecs, Erreurs)
- 🔍 Filtres (Action, Utilisateur, Statut)
- 📋 Table détaillée des logs
- 🔄 Actualisation en temps réel
- 🗑️ Effacement des logs

## 🚀 Utilisation

### Côté Backend - Logger une action

```java
// Option 1 : Utiliser directement le service
@Autowired
private AuditLogService auditLogService;

auditLogService.logAction(
    "CREATE_USER",        // Action
    "User",              // Type d'entité
    userId.toString(),   // ID de l'entité
    null,               // Ancienne valeur
    newUser.toString(), // Nouvelle valeur
    "Utilisateur créé avec succès"  // Message
);

// Option 2 : Utiliser l'annotation @Auditable
@Auditable(action = "CREATE_USER", entityType = "User")
public User createUser(UserDTO userDTO) {
    // La méthode sera automatiquement loggée
    return userRepository.save(user);
}
```

### Logger une authentification

```java
auditLogService.logAuthentication(
    username,
    "SUCCESS",  // ou "FAILED"
    "Connexion réussie",
    ipAddress,
    userAgent
);
```

### Logger une erreur

```java
auditLogService.logError(
    "PROCESS_PAYMENT",
    "Payment",
    "Erreur lors du traitement du paiement",
    exception.getMessage()
);
```

### Côté Frontend - Afficher les logs

```typescript
// Charger tous les logs
this.auditLogService.getAllLogs().subscribe(logs => {
  this.auditLogs = logs;
});

// Filtrer les logs
this.auditLogService.getFilteredLogs(
  'AUTHENTICATION',  // action
  'admin@example',   // username
  null,             // startDate
  null              // endDate
).subscribe(logs => {
  this.filteredLogs = logs;
});

// Effacer tous les logs
this.auditLogService.clearLogs().subscribe(() => {
  console.log('Logs effacés');
});
```

## ⚙️ Configuration

### Rotation des logs
Le système limite automatiquement le fichier à **10 000 logs maximum**. Quand cette limite est atteinte, seuls les 80% les plus récents sont conservés.

### Emplacement du fichier
- **Chemin** : `logs/audit.log`
- **Format** : Une ligne JSON par log
- **Encodage** : UTF-8

### Sécurité
- ✅ Seuls les administrateurs peuvent accéder aux logs (`@PreAuthorize("hasRole('ADMIN')")`)
- ✅ Les logs sont immuables (pas de modification possible)
- ✅ L'effacement des logs est lui-même loggé

## 📈 Bonnes Pratiques

1. **Logs réguliers** : Logger toutes les actions critiques
2. **Messages clairs** : Utiliser des messages descriptifs
3. **Données sensibles** : Ne pas logger de mots de passe ou données bancaires
4. **Performance** : Le logging est asynchrone et n'impacte pas les performances
5. **Archivage** : Prévoir une stratégie d'archivage des anciens logs

## 🔒 Conformité RGPD

- Les données personnelles dans les logs sont minimales
- Possibilité d'anonymiser/pseudonymiser les données sensibles
- Durée de rétention configurable
- Droit à l'effacement respecté

## 📝 Exemple de Logs

```
{"timestamp":"2025-11-03T14:23:45.123","username":"admin@poste.tg","userRole":"ROLE_ADMIN","action":"AUTHENTICATION","ipAddress":"192.168.1.100","userAgent":"Mozilla/5.0","status":"SUCCESS","message":"Connexion réussie"}
{"timestamp":"2025-11-03T14:25:12.456","userId":"12","username":"admin@poste.tg","userRole":"ROLE_ADMIN","action":"CREATE","entityType":"User","entityId":"45","ipAddress":"192.168.1.100","status":"SUCCESS","message":"Action CREATE réussie sur User"}
{"timestamp":"2025-11-03T14:30:00.789","username":"operator@poste.tg","action":"AUTHENTICATION","ipAddress":"192.168.1.105","status":"FAILED","message":"Identifiants invalides"}
```

## 🆘 Dépannage

### Les logs ne s'affichent pas dans l'interface
1. Vérifier que le backend est démarré
2. Vérifier les permissions (rôle ADMIN requis)
3. Vérifier la console du navigateur pour les erreurs
4. Vérifier que le fichier `logs/audit.log` existe

### Erreur lors de l'enregistrement des logs
1. Vérifier les permissions d'écriture sur le dossier `logs/`
2. Vérifier l'espace disque disponible
3. Vérifier les logs applicatifs Spring Boot

## 📞 Support

Pour toute question ou problème, contactez l'équipe de développement.

---

**Version** : 1.0  
**Dernière mise à jour** : 3 novembre 2025
