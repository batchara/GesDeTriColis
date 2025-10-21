-- Fix pour la colonne token qui est trop courte
-- Le token JWT peut faire jusqu'à 500+ caractères avec les claims personnalisés

-- Solution 1 : Augmenter la taille à 1000 caractères (recommandé)
ALTER TABLE tokens 
ALTER COLUMN token TYPE VARCHAR(1000);

-- Solution 2 : Utiliser TEXT (illimité)
-- ALTER TABLE tokens 
-- ALTER COLUMN token TYPE TEXT;

-- Vérifier la modification
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'tokens' AND column_name = 'token';
