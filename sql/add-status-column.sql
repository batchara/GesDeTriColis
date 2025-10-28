-- Script pour ajouter la colonne status à la table agences
-- Date: 2025

-- Ajout de la colonne status
ALTER TABLE agences 
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';

-- Mise à jour de toutes les agences existantes pour avoir le statut ACTIVE
UPDATE agences 
SET status = 'ACTIVE' 
WHERE status IS NULL;

-- Vérification
SELECT id, code, label AS nom, region, status 
FROM agences 
ORDER BY region, label;

-- Statistiques par statut
SELECT status, COUNT(*) as nombre 
FROM agences 
GROUP BY status;
