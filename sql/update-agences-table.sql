-- Script de mise à jour de la table agences
-- Date: 2025-10-28

-- 1. Sauvegarder les données existantes
CREATE TEMP TABLE agences_backup AS 
SELECT id, code, email, 
       COALESCE(num_tel, '') as tel,
       region, label,
       latitude, longitude,
       adresse_complete,
       created_date, last_modified_date
FROM agences;

-- 2. Supprimer l'ancienne table
DROP TABLE IF EXISTS agences CASCADE;

-- 3. Recréer la table avec la structure correcte
CREATE TABLE agences (
    id SERIAL PRIMARY KEY,
    code VARCHAR(255),
    email VARCHAR(255),
    num_tel VARCHAR(255),
    region VARCHAR(100),
    label VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    adresse_complete VARCHAR(300) NOT NULL,
    created_date TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP(6) WITH TIME ZONE
);

-- 4. Créer les index
CREATE INDEX idx_agences_code ON agences(code);
CREATE INDEX idx_agences_region ON agences(region);

-- 5. Restaurer les données
INSERT INTO agences (
    id, code, email, num_tel, region, label,
    latitude, longitude, adresse_complete,
    created_date, last_modified_date
)
SELECT 
    id, code, email, tel, region, label,
    latitude, longitude, adresse_complete,
    created_date, last_modified_date
FROM agences_backup;

-- 6. Réinitialiser la séquence
SELECT setval('agences_id_seq', (SELECT COALESCE(MAX(id), 1) FROM agences));

-- 7. Vérifier le résultat
SELECT COUNT(*) as total_agences FROM agences;
\echo '\n=== Structure de la table agences ==='
\d agences
\echo '\n=== Agences existantes ==='
SELECT id, code, label, region, email FROM agences;
