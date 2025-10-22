-- ============================================
-- Migration : Fix colonne token trop courte
-- ============================================
-- Le token JWT peut faire jusqu'à 500+ caractères avec les claims personnalisés
-- Par défaut JPA crée VARCHAR(255) qui est insuffisant

-- 1. Vérifier la taille actuelle
SELECT 
    table_name,
    column_name, 
    data_type, 
    character_maximum_length as max_length
FROM information_schema.columns 
WHERE table_name = 'tokens' 
  AND column_name = 'token';

-- 2. Modifier la taille à 1000 caractères
ALTER TABLE tokens 
ALTER COLUMN token TYPE VARCHAR(1000);

-- 3. Vérifier la modification
SELECT 
    table_name,
    column_name, 
    data_type, 
    character_maximum_length as max_length
FROM information_schema.columns 
WHERE table_name = 'tokens' 
  AND column_name = 'token';

-- 4. Afficher les tokens existants (tronqués pour la lisibilité)
SELECT 
    id, 
    user_id,
    LENGTH(token) as token_length,
    SUBSTRING(token, 1, 50) || '...' as token_preview,
    created_at,
    expires_at
FROM tokens
ORDER BY created_at DESC
LIMIT 5;
