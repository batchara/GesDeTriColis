-- ============================================================================
-- Script de vérification que le soft delete fonctionne correctement
-- ============================================================================

-- 1. Voir le nombre total de colis (incluant supprimés)
SELECT 
    '1. TOTAL COLIS (incluant supprimés)' as description,
    COUNT(*) as nombre
FROM colis;

-- 2. Voir le nombre de colis ACTIFS (non supprimés)
SELECT 
    '2. COLIS ACTIFS (non supprimés)' as description,
    COUNT(*) as nombre
FROM colis
WHERE deleted IS NULL OR deleted = false;

-- 3. Voir le nombre de colis SUPPRIMÉS
SELECT 
    '3. COLIS SUPPRIMÉS' as description,
    COUNT(*) as nombre
FROM colis
WHERE deleted = true;

-- 4. Détails des colis supprimés (les 10 derniers)
SELECT 
    '4. DERNIERS COLIS SUPPRIMÉS' as description;
    
SELECT 
    id,
    code_suivi,
    nom_dest,
    statut,
    deleted,
    deleted_at,
    deleted_by
FROM colis
WHERE deleted = true
ORDER BY deleted_at DESC
LIMIT 10;

-- 5. Vérifier qu'aucun colis supprimé n'est retourné par la requête "active"
-- (Cette requête devrait retourner 0 lignes)
SELECT 
    '5. TEST: Colis supprimés dans requête ACTIVE (devrait être 0)' as description;
    
SELECT 
    id,
    code_suivi,
    deleted
FROM colis
WHERE deleted = true
  AND (deleted IS NULL OR deleted = false); -- Cette condition devrait filtrer tout

-- 6. Statistiques de suppression par utilisateur
SELECT 
    '6. STATISTIQUES DE SUPPRESSION PAR UTILISATEUR' as description;
    
SELECT 
    deleted_by as utilisateur,
    COUNT(*) as nombre_suppressions,
    MIN(deleted_at) as premiere_suppression,
    MAX(deleted_at) as derniere_suppression
FROM colis
WHERE deleted = true
GROUP BY deleted_by
ORDER BY nombre_suppressions DESC;

-- 7. Statistiques de suppression par jour
SELECT 
    '7. STATISTIQUES DE SUPPRESSION PAR JOUR' as description;
    
SELECT 
    DATE(deleted_at) as jour,
    COUNT(*) as nombre_suppressions
FROM colis
WHERE deleted = true
GROUP BY DATE(deleted_at)
ORDER BY jour DESC
LIMIT 7;

-- ============================================================================
-- COMMANDES DE RESTAURATION (si nécessaire)
-- ============================================================================

-- Pour restaurer UN colis supprimé par erreur :
-- UPDATE colis 
-- SET deleted = false, deleted_at = NULL, deleted_by = NULL 
-- WHERE id = <ID_DU_COLIS>;

-- Pour restaurer TOUS les colis supprimés aujourd'hui :
-- UPDATE colis 
-- SET deleted = false, deleted_at = NULL, deleted_by = NULL 
-- WHERE deleted = true 
-- AND DATE(deleted_at) = CURRENT_DATE;

-- ============================================================================
-- SUPPRESSION DÉFINITIVE (DANGER!)
-- ============================================================================

-- ⚠️ ATTENTION: Cette commande supprime PHYSIQUEMENT les colis de la base
-- ⚠️ Cette action est IRRÉVERSIBLE
-- ⚠️ À utiliser UNIQUEMENT si vous êtes ABSOLUMENT SÛR

-- Pour supprimer physiquement les colis soft-deleted depuis plus de 30 jours :
-- DELETE FROM colis 
-- WHERE deleted = true 
-- AND deleted_at < NOW() - INTERVAL '30 days';

-- Pour supprimer physiquement UN colis spécifique :
-- DELETE FROM colis WHERE id = <ID_DU_COLIS>;
