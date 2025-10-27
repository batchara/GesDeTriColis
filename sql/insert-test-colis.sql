-- Insertion de données de test pour les colis
-- Statuts valides: EN_ATTENTE, AFFECTE, EXPEDIE, RECEPTIONNE, LIVRE, ANNULEE

INSERT INTO colis (code_suivi, poids, nom_exp, nom_dest, tel_dest, adresse_dest, date_envoi, date_prevue, statut, created_date, last_modified_date) 
VALUES 
('COL-20251027-ABC123', 2.5, 'Kodjo Mensah', 'Afi Kouamé', '+228 90 11 22 33', 'Rue du Commerce, Quartier Agbalépédo, Lomé', NOW(), NOW() + INTERVAL '3 days', 'EXPEDIE', NOW(), NOW()),
('COL-20251027-DEF456', 1.2, 'Yao Tossou', 'Kokou Amegavi', '+228 91 33 44 55', 'Avenue des Savanes, Centre Ville, Kara', NOW(), NOW() + INTERVAL '4 days', 'EN_ATTENTE', NOW(), NOW()),
('COL-20251027-GHI789', 5.8, 'Edem Koffi', 'Séna Akakpo', '+228 92 55 66 77', 'Place du Marché Central, Sokodé', NOW(), NOW() + INTERVAL '5 days', 'AFFECTE', NOW(), NOW()),
('COL-20251027-JKL012', 0.5, 'Ablavi Dzigbodi', 'Koffi Agbeko', '+228 93 77 88 99', 'Boulevard Gnassingbé Eyadéma, Tokoin, Lomé', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', 'LIVRE', NOW(), NOW()),
('COL-20251027-MNO345', 3.2, 'Kossi Agbodjan', 'Amavi Lawson', '+228 94 11 00 22', 'Adresse introuvable', NOW() - INTERVAL '5 days', NOW() - INTERVAL '3 days', 'RECEPTIONNE', NOW(), NOW());
