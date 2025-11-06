-- Script de nettoyage de la base de données
-- Date: 4 novembre 2025
-- Description: Suppression des tokens orphelins et nettoyage général

-- =============================================================================
-- ÉTAPE 1: Vérification des tokens orphelins (utilisateurs supprimés)
-- =============================================================================
SELECT 
    t.id AS token_id,
    t.token,
    t.user_id,
    t.created_at,
    t.expires_at,
    'TOKEN ORPHELIN' AS status
FROM tokens t
LEFT JOIN utilisateurs u ON t.user_id = u.id
WHERE u.id IS NULL;

-- =============================================================================
-- ÉTAPE 2: Suppression des tokens orphelins
-- =============================================================================
DELETE FROM tokens
WHERE user_id NOT IN (SELECT id FROM utilisateurs);

-- =============================================================================
-- ÉTAPE 3: Suppression des tokens expirés (plus de 24h)
-- =============================================================================
DELETE FROM tokens
WHERE expires_at < NOW() - INTERVAL '24 hours';

-- =============================================================================
-- ÉTAPE 4: Suppression des anciens tokens d'activation (plus de 7 jours)
-- =============================================================================
DELETE FROM tokens
WHERE created_at < NOW() - INTERVAL '7 days'
  AND token_type IS NULL; -- tokens d'activation

-- =============================================================================
-- ÉTAPE 5: Vérification après nettoyage
-- =============================================================================
SELECT 
    'Total tokens restants' AS description,
    COUNT(*) AS count
FROM tokens
UNION ALL
SELECT 
    'Tokens expirés' AS description,
    COUNT(*) AS count
FROM tokens
WHERE expires_at < NOW()
UNION ALL
SELECT 
    'Tokens valides' AS description,
    COUNT(*) AS count
FROM tokens
WHERE expires_at >= NOW();

-- =============================================================================
-- ÉTAPE 6: Statistiques par utilisateur
-- =============================================================================
SELECT 
    u.email,
    u.nom,
    u.prenom,
    u.enabled,
    u.must_change_password,
    COUNT(t.id) AS nombre_tokens
FROM utilisateurs u
LEFT JOIN tokens t ON u.id = t.user_id
GROUP BY u.id, u.email, u.nom, u.prenom, u.enabled, u.must_change_password
ORDER BY nombre_tokens DESC;

-- =============================================================================
-- BONUS: Suppression des tokens en doublon pour un même utilisateur
-- (garde seulement le plus récent pour chaque utilisateur)
-- =============================================================================
DELETE FROM tokens
WHERE id NOT IN (
    SELECT MAX(id)
    FROM tokens
    GROUP BY user_id
);

-- =============================================================================
-- Résumé final
-- =============================================================================
SELECT 
    'Nettoyage terminé' AS status,
    NOW() AS timestamp;
