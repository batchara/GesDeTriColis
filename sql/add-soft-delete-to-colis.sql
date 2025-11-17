-- ==========================================
-- Script SQL: Ajout des colonnes de soft delete à la table colis
-- Date: 13 novembre 2025
-- Description: Permet de marquer les colis comme supprimés sans les supprimer physiquement
-- ==========================================

-- 1. Ajouter la colonne 'deleted' (NULL par défaut pour compatibilité)
ALTER TABLE colis 
ADD COLUMN IF NOT EXISTS deleted BOOLEAN;

-- 2. Ajouter la colonne 'deleted_at' (timestamp de suppression)
ALTER TABLE colis 
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- 3. Ajouter la colonne 'deleted_by' (qui a supprimé le colis)
ALTER TABLE colis 
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);

-- 4. Mettre à jour les colis existants pour qu'ils soient marqués comme non supprimés
UPDATE colis SET deleted = FALSE WHERE deleted IS NULL;

-- 5. Créer un index sur la colonne 'deleted' pour optimiser les requêtes
CREATE INDEX IF NOT EXISTS idx_colis_deleted ON colis(deleted);

-- 6. Vérification
SELECT 
    COUNT(*) as total_colis,
    COUNT(CASE WHEN deleted = TRUE THEN 1 END) as colis_supprimes,
    COUNT(CASE WHEN deleted = FALSE THEN 1 END) as colis_actifs
FROM colis;

-- ==========================================
-- NOTES:
-- - Les colis marqués comme 'deleted=true' ne seront plus affichés dans les interfaces
-- - Ils restent dans la base de données pour traçabilité
-- - Pour voir les colis supprimés : SELECT * FROM colis WHERE deleted = TRUE;
-- - Pour restaurer un colis : UPDATE colis SET deleted = FALSE WHERE id = XXX;
-- ==========================================
