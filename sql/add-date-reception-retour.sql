-- Ajout des colonnes pour le suivi des dates de transition des statuts
-- Et gestion du retour automatique après 30 jours

-- Ajouter la colonne date_reception (date à laquelle le colis est réceptionné à l'agence)
ALTER TABLE colis ADD COLUMN IF NOT EXISTS date_reception TIMESTAMP;

-- Ajouter la colonne date_retour (date à laquelle le colis passe en statut RETOUR)
ALTER TABLE colis ADD COLUMN IF NOT EXISTS date_retour TIMESTAMP;

-- Mettre à jour la date de réception pour les colis déjà réceptionnés
-- En utilisant la date de dernière modification comme approximation
UPDATE colis 
SET date_reception = last_modified_date 
WHERE statut = 'RECEPTIONNE' 
  AND date_reception IS NULL
  AND last_modified_date IS NOT NULL;

-- Mettre à jour la date de retour pour les colis déjà en statut RETOUR
UPDATE colis 
SET date_retour = last_modified_date 
WHERE statut = 'RETOUR' 
  AND date_retour IS NULL
  AND last_modified_date IS NOT NULL;

-- Créer un index pour optimiser les requêtes du scheduler
CREATE INDEX IF NOT EXISTS idx_colis_statut_date_reception 
ON colis(statut, date_reception);

-- Vérification
SELECT 
    COUNT(*) as total_colis,
    COUNT(date_reception) as avec_date_reception,
    COUNT(date_retour) as avec_date_retour
FROM colis;

COMMENT ON COLUMN colis.date_reception IS 'Date et heure de réception du colis à l''agence (statut RECEPTIONNE)';
COMMENT ON COLUMN colis.date_retour IS 'Date et heure du passage au statut RETOUR (manuel ou automatique après 30 jours)';
