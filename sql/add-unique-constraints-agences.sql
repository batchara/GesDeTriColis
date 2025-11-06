-- Script pour ajouter les contraintes d'unicité sur la table agences
-- Date: 2025-11-05
-- Description: Ajoute les contraintes UNIQUE sur les colonnes code et label pour garantir l'unicité

-- Étape 1: Vérifier s'il existe des doublons avant d'ajouter les contraintes
-- Vérification des doublons sur le code
SELECT code, COUNT(*) as count
FROM agences
WHERE code IS NOT NULL
GROUP BY code
HAVING COUNT(*) > 1;

-- Vérification des doublons sur le label (nom)
SELECT label, COUNT(*) as count
FROM agences
WHERE label IS NOT NULL
GROUP BY label
HAVING COUNT(*) > 1;

-- Étape 2: Si des doublons existent, les corriger manuellement avant d'exécuter les contraintes
-- Exemple de correction (à adapter selon vos données):
-- UPDATE agences SET code = 'CODE_UNIQUE_1' WHERE id = <id_du_doublon>;
-- UPDATE agences SET label = 'LABEL_UNIQUE_1' WHERE id = <id_du_doublon>;

-- Étape 3: Ajouter la contrainte NOT NULL sur le code si nécessaire
ALTER TABLE agences MODIFY COLUMN code VARCHAR(255) NOT NULL;

-- Étape 4: Ajouter la contrainte NOT NULL sur le label si nécessaire
ALTER TABLE agences MODIFY COLUMN label VARCHAR(100) NOT NULL;

-- Étape 5: Ajouter la contrainte UNIQUE sur le code
ALTER TABLE agences ADD CONSTRAINT uk_agences_code UNIQUE (code);

-- Étape 6: Ajouter la contrainte UNIQUE sur le label
ALTER TABLE agences ADD CONSTRAINT uk_agences_label UNIQUE (label);

-- Étape 7: Vérifier que les contraintes ont été ajoutées
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu 
    ON tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
WHERE tc.TABLE_NAME = 'agences' 
    AND tc.CONSTRAINT_TYPE = 'UNIQUE';
