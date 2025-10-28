-- Script de mise à jour de la table colis
-- Date: 2025-10-28

-- 1. Sauvegarder les données existantes
CREATE TEMP TABLE colis_backup AS 
SELECT id, code_suivi, poids, 
       COALESCE(nom_exp, nom_expediteur) as nom_exp, 
       COALESCE(nom_dest, nom_destinataire) as nom_dest, 
       COALESCE(tel_dest, tel_destinataire) as tel_dest, 
       date_envoi, date_prevue, statut, 
       COALESCE(adresse_dest, adressedest, adresse_detectee) as adresse_dest,
       operateur_id, centre_tri_id, agence_affectee_id,
       created_date, last_modified_date
FROM colis;

-- 2. Supprimer l'ancienne table
DROP TABLE IF EXISTS colis CASCADE;

-- 3. Recréer la table avec la structure correcte
CREATE TABLE colis (
    id SERIAL PRIMARY KEY,
    code_suivi VARCHAR(255) UNIQUE NOT NULL,
    poids NUMERIC(38,2),
    nom_exp VARCHAR(255),
    nom_dest VARCHAR(255) NOT NULL,
    tel_dest VARCHAR(255),
    date_envoi TIMESTAMP(6) WITH TIME ZONE,
    date_prevue TIMESTAMP(6) WITH TIME ZONE,
    statut VARCHAR(255) NOT NULL,
    adresse_dest VARCHAR(500),
    operateur_id INTEGER,
    centre_tri_id INTEGER,
    agence_affectee_id INTEGER,
    created_date TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP(6) WITH TIME ZONE
);

-- 4. Créer les index
CREATE INDEX idx_colis_code_suivi ON colis(code_suivi);
CREATE INDEX idx_colis_statut ON colis(statut);
CREATE INDEX idx_colis_operateur ON colis(operateur_id);
CREATE INDEX idx_colis_centre_tri ON colis(centre_tri_id);
CREATE INDEX idx_colis_agence ON colis(agence_affectee_id);

-- 5. Restaurer les données
INSERT INTO colis (
    id, code_suivi, poids, nom_exp, nom_dest, tel_dest,
    date_envoi, date_prevue, statut, adresse_dest,
    operateur_id, centre_tri_id, agence_affectee_id,
    created_date, last_modified_date
)
SELECT 
    id, code_suivi, poids, nom_exp, nom_dest, tel_dest,
    date_envoi, date_prevue, statut, adresse_dest,
    operateur_id, centre_tri_id, agence_affectee_id,
    created_date, last_modified_date
FROM colis_backup;

-- 6. Réinitialiser la séquence
SELECT setval('colis_id_seq', (SELECT COALESCE(MAX(id), 1) FROM colis));

-- 7. Vérifier le résultat
SELECT COUNT(*) as total_colis FROM colis;
\echo '\n=== Structure de la table colis ==='
\d colis
\echo '\n=== 5 premiers colis ==='
SELECT id, code_suivi, nom_exp, nom_dest, statut FROM colis LIMIT 5;
