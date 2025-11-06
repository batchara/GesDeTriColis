-- Script de nettoyage COMPLET de la base de données
-- Date: 4 novembre 2025
-- ⚠️ ATTENTION: Ce script est plus agressif et supprime beaucoup de données
-- À utiliser uniquement si vous voulez repartir à zéro

-- =============================================================================
-- ÉTAPE 1: Vérification de l'état actuel
-- =============================================================================
SELECT 'AVANT NETTOYAGE' AS phase;

SELECT 'Utilisateurs' AS table_name, COUNT(*) AS count FROM utilisateurs
UNION ALL
SELECT 'Tokens' AS table_name, COUNT(*) AS count FROM tokens
UNION ALL
SELECT 'Notifications' AS table_name, COUNT(*) AS count FROM notifications;

-- =============================================================================
-- ÉTAPE 2: Suppression de TOUS les tokens
-- (Tous les utilisateurs devront se reconnecter)
-- =============================================================================
TRUNCATE TABLE tokens CASCADE;

-- =============================================================================
-- ÉTAPE 3: Réinitialisation du flag mustChangePassword pour duranraxi
-- (pour pouvoir tester la nouvelle fonctionnalité)
-- =============================================================================
UPDATE utilisateurs
SET must_change_password = true
WHERE email = 'duranraxi@gmail.com';

-- =============================================================================
-- ÉTAPE 4: Suppression des utilisateurs non activés (enabled=false)
-- créés il y a plus de 7 jours
-- =============================================================================
DELETE FROM utilisateurs
WHERE enabled = false
  AND created_date < NOW() - INTERVAL '7 days';

-- =============================================================================
-- ÉTAPE 5: Suppression des anciennes notifications (plus de 30 jours)
-- =============================================================================
DELETE FROM notifications
WHERE created_at < NOW() - INTERVAL '30 days';

-- =============================================================================
-- ÉTAPE 6: Vérification après nettoyage
-- =============================================================================
SELECT 'APRÈS NETTOYAGE' AS phase;

SELECT 'Utilisateurs' AS table_name, COUNT(*) AS count FROM utilisateurs
UNION ALL
SELECT 'Tokens' AS table_name, COUNT(*) AS count FROM tokens
UNION ALL
SELECT 'Notifications' AS table_name, COUNT(*) AS count FROM notifications;

-- =============================================================================
-- ÉTAPE 7: Liste des utilisateurs restants
-- =============================================================================
SELECT 
    id,
    email,
    nom,
    prenom,
    enabled AS compte_active,
    must_change_password AS doit_changer_mdp,
    account_locked AS compte_bloque,
    created_date AS date_creation
FROM utilisateurs
ORDER BY created_date DESC;

-- =============================================================================
-- Résumé final
-- =============================================================================
SELECT 
    'Nettoyage COMPLET terminé' AS status,
    'Tous les tokens supprimés - Reconnexion requise' AS note,
    NOW() AS timestamp;
