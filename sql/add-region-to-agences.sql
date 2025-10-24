-- Script pour ajouter la colonne region à la table agences si elle n'existe pas

-- Vérifier si la colonne existe déjà
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'agences' 
        AND column_name = 'region'
    ) THEN
        -- Ajouter la colonne region
        ALTER TABLE agences ADD COLUMN region VARCHAR(100);
        RAISE NOTICE 'Colonne region ajoutée à la table agences';
    ELSE
        RAISE NOTICE 'La colonne region existe déjà dans la table agences';
    END IF;
END $$;

-- Vérifier la structure de la table
SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' 
AND table_name = 'agences'
ORDER BY ordinal_position;
