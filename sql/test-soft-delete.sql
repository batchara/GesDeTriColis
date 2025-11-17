-- Script de test pour vérifier le soft delete des colis

-- 1. Afficher tous les colis (même supprimés)
SELECT id, code_suivi, nom_dest, deleted, deleted_at, deleted_by
FROM colis
ORDER BY id DESC
LIMIT 10;

-- 2. Afficher uniquement les colis ACTIFS (non supprimés)
SELECT id, code_suivi, nom_dest, deleted, deleted_at, deleted_by
FROM colis
WHERE deleted IS NULL OR deleted = false
ORDER BY id DESC
LIMIT 10;

-- 3. Afficher uniquement les colis SUPPRIMÉS
SELECT id, code_suivi, nom_dest, deleted, deleted_at, deleted_by
FROM colis
WHERE deleted = true
ORDER BY deleted_at DESC
LIMIT 10;

-- 4. Compter les colis par statut de suppression
SELECT 
    CASE 
        WHEN deleted IS NULL THEN 'NULL (jamais défini)'
        WHEN deleted = true THEN 'SUPPRIMÉ'
        WHEN deleted = false THEN 'ACTIF'
    END as statut_suppression,
    COUNT(*) as nombre
FROM colis
GROUP BY deleted
ORDER BY deleted NULLS FIRST;

-- 5. Pour supprimer VRAIMENT un colis en dur (ATTENTION: irréversible!)
-- DELETE FROM colis WHERE id = ?;

-- 6. Pour restaurer un colis supprimé (annuler le soft delete)
-- UPDATE colis SET deleted = false, deleted_at = NULL, deleted_by = NULL WHERE id = ?;
