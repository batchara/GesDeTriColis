-- Script d'insertion de boîtes postales de test
-- À exécuter après la création de la table boites_postales

-- Insertion de boîtes postales pour différentes agences (en supposant que les agences existent)

-- Boîtes postales pour l'agence 1
INSERT INTO boites_postales (libelle, capacite, agence_id, created_by) VALUES
('Boîte Postale Standard 1', 50, 1, 'SYSTEM'),
('Boîte Postale Standard 2', 50, 1, 'SYSTEM'),
('Boîte Postale Grande Capacité', 100, 1, 'SYSTEM'),
('Boîte Postale Standard 3', 50, 1, 'SYSTEM'),
('Boîte Postale XL', 150, 1, 'SYSTEM');

-- Boîtes postales pour l'agence 2
INSERT INTO boites_postales (libelle, capacite, agence_id, created_by) VALUES
('Boîte Postale Standard 1', 50, 2, 'SYSTEM'),
('Boîte Postale Standard 2', 50, 2, 'SYSTEM'),
('Boîte Postale Grande Capacité', 100, 2, 'SYSTEM'),
('Boîte Postale Standard 3', 50, 2, 'SYSTEM');

-- Boîtes postales pour l'agence 3
INSERT INTO boites_postales (libelle, capacite, agence_id, created_by) VALUES
('Boîte Postale Standard 1', 50, 3, 'SYSTEM'),
('Boîte Postale Grande Capacité', 100, 3, 'SYSTEM'),
('Boîte Postale XL', 150, 3, 'SYSTEM');

-- Vérification des insertions
SELECT COUNT(*) as total_boites_postales FROM boites_postales WHERE deleted_at IS NULL;
SELECT agence_id, COUNT(*) as nombre_boites FROM boites_postales WHERE deleted_at IS NULL GROUP BY agence_id;
