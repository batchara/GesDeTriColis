-- ============================================================
-- 🔒 Script de migration: Ajout des colonnes de sécurité
-- Gestion des tentatives de connexion échouées
-- ============================================================

-- Ajout des colonnes pour le suivi des tentatives de connexion
ALTER TABLE utilisateurs 
ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0 NOT NULL,
ADD COLUMN IF NOT EXISTS last_failed_login DATE,
ADD COLUMN IF NOT EXISTS lock_time DATE;

-- Mise à jour des valeurs par défaut pour les utilisateurs existants
UPDATE utilisateurs 
SET failed_login_attempts = 0 
WHERE failed_login_attempts IS NULL;

-- Vérification
SELECT 
    email,
    account_locked,
    failed_login_attempts,
    last_failed_login,
    lock_time
FROM utilisateurs
ORDER BY email;

-- ============================================================
-- Notes:
-- 1. failed_login_attempts: Compteur de tentatives échouées (max 3)
-- 2. last_failed_login: Date de la dernière tentative échouée
-- 3. lock_time: Date de verrouillage du compte
-- 
-- Sécurité:
-- - Après 3 tentatives échouées, le compte est automatiquement verrouillé
-- - L'admin peut débloquer via l'endpoint /users/{id}/unlock
-- - L'utilisateur peut demander une réinitialisation de mot de passe
-- ============================================================
