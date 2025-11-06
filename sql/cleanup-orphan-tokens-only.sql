-- Script rapide: Suppression des tokens orphelins uniquement
-- Date: 4 novembre 2025
-- Sécuritaire: Supprime seulement les tokens des utilisateurs supprimés

-- Vérifier les tokens orphelins
SELECT 
    t.id,
    t.user_id,
    t.created_at,
    'ORPHELIN - Utilisateur inexistant' AS status
FROM tokens t
LEFT JOIN utilisateurs u ON t.user_id = u.id
WHERE u.id IS NULL;

-- Suppression des tokens orphelins
DELETE FROM tokens
WHERE user_id NOT IN (SELECT id FROM utilisateurs);

-- Vérification
SELECT 
    COUNT(*) AS tokens_restants
FROM tokens;
