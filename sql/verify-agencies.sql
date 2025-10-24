-- ============================================
-- Script de vérification complète des agences
-- ============================================

-- 1. Vérifier la structure de la table agences
SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' 
AND table_name = 'agences'
ORDER BY ordinal_position;

-- 2. Vérifier le nombre total d'agences
SELECT COUNT(*) AS total_agences FROM agences;

-- 3. Afficher toutes les agences avec leurs détails complets
SELECT 
    a.id,
    a.created_date,
    a.label AS nom_agence,
    a.code,
    a.email,
    a.num_tel,
    a.region,
    a.adresse_id,
    ad.adresse_complete,
    ad.quartier,
    ad.ville,
    ad.code_postal,
    ad.latitude,
    ad.longitude,
    ad.type_adresse
FROM agences a
LEFT JOIN adresses ad ON a.adresse_id = ad.id
ORDER BY a.created_date DESC;

-- 4. Vérifier les agences par région
SELECT 
    a.region,
    COUNT(*) AS nombre_agences,
    STRING_AGG(a.label, ', ') AS liste_agences
FROM agences a
GROUP BY a.region
ORDER BY nombre_agences DESC;

-- 5. Vérifier les agences sans email (optionnel)
SELECT 
    a.id,
    a.label,
    a.code,
    a.email,
    CASE 
        WHEN a.email IS NULL OR a.email = '' THEN 'Pas d''email'
        ELSE 'Email présent'
    END AS statut_email
FROM agences a
ORDER BY a.created_date DESC;

-- 6. Statistiques sur les adresses
SELECT 
    COUNT(DISTINCT a.adresse_id) AS adresses_uniques,
    COUNT(CASE WHEN ad.latitude IS NOT NULL AND ad.longitude IS NOT NULL THEN 1 END) AS avec_coordonnees_gps,
    COUNT(CASE WHEN ad.quartier IS NOT NULL THEN 1 END) AS avec_quartier,
    COUNT(CASE WHEN ad.code_postal IS NOT NULL THEN 1 END) AS avec_code_postal
FROM agences a
LEFT JOIN adresses ad ON a.adresse_id = ad.id;

-- ============================================
-- Pour tester l'API REST depuis SQL
-- ============================================

-- Note: Ces requêtes simulent ce que l'API devrait retourner
-- Format JSON similaire à AgenceDTO

SELECT json_build_object(
    'id', a.id,
    'nom', a.label,
    'code', a.code,
    'email', a.email,
    'numTel', a.num_tel,
    'region', a.region,
    'status', 'ACTIVE',
    'adresse', json_build_object(
        'id', ad.id,
        'adresseComplete', ad.adresse_complete,
        'quartier', ad.quartier,
        'ville', ad.ville,
        'codePostale', ad.code_postal,
        'typeAdresse', ad.type_adresse,
        'latitude', ad.latitude,
        'longitude', ad.longitude
    )
) AS agence_json
FROM agences a
LEFT JOIN adresses ad ON a.adresse_id = ad.id
ORDER BY a.created_date DESC;
