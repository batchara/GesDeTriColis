-- Script pour insérer une agence de test dans la base de données
-- Ce script insère d'abord une adresse, puis une agence qui référence cette adresse

-- 1. Insérer l'adresse d'abord
INSERT INTO adresses (
    created_date,
    last_modified_date,
    adresse_complete,
    rue,
    type_adresse,
    latitude,
    longitude,
    quartier,
    ville,
    code_postal
) VALUES (
    NOW(),
    NOW(),
    'Avenue de la Victoire, Immeuble La Poste',
    'Avenue de la Victoire',
    'BUREAU',
    6.1319,
    1.2223,
    'Nyékonakpoè',
    'Lomé',
    '01BP3000'
) RETURNING id;

-- Note: Récupérez l'ID retourné ci-dessus pour l'utiliser dans la commande suivante
-- Remplacez <ADRESSE_ID> par l'ID obtenu

-- 2. Insérer l'agence avec l'ID de l'adresse
-- IMPORTANT: Remplacez <ADRESSE_ID> par l'ID réel obtenu à l'étape 1
INSERT INTO agences (
    created_date,
    last_modified_date,
    label,
    code,
    email,
    num_tel,
    region,
    adresse_id
) VALUES (
    NOW(),
    NOW(),
    'Agence Lomé Nyékonakpoè',
    'LME-NYE-001',
    'nyekonakpoe@poste.tg',
    '+228 22 21 55 66',
    'Maritime',
    <ADRESSE_ID>  -- Remplacez par l'ID de l'adresse
);

-- ============================================
-- VERSION ALTERNATIVE: Insertion en une seule transaction
-- ============================================

-- Supprimez les insertions ci-dessus et utilisez cette version si vous préférez :

DO $$
DECLARE
    v_adresse_id INTEGER;
BEGIN
    -- Insérer l'adresse
    INSERT INTO adresses (
        created_date,
        last_modified_date,
        adresse_complete,
        rue,
        type_adresse,
        latitude,
        longitude,
        quartier,
        ville,
        code_postal
    ) VALUES (
        NOW(),
        NOW(),
        'Avenue de la Victoire, Immeuble La Poste',
        'Avenue de la Victoire',
        'BUREAU',
        6.1319,
        1.2223,
        'Nyékonakpoè',
        'Lomé',
        '01BP3000'
    ) RETURNING id INTO v_adresse_id;

    -- Insérer l'agence avec l'ID de l'adresse
    INSERT INTO agences (
        created_date,
        last_modified_date,
        label,
        code,
        email,
        num_tel,
        region,
        adresse_id
    ) VALUES (
        NOW(),
        NOW(),
        'Agence Lomé Nyékonakpoè',
        'LME-NYE-001',
        'nyekonakpoe@poste.tg',
        '+228 22 21 55 66',
        'Maritime',
        v_adresse_id
    );

    RAISE NOTICE 'Agence créée avec succès avec l''adresse ID: %', v_adresse_id;
END $$;

-- ============================================
-- Vérification: Afficher toutes les agences
-- ============================================

SELECT 
    a.id,
    a.label AS nom_agence,
    a.code,
    a.email,
    a.num_tel,
    a.region,
    ad.adresse_complete,
    ad.quartier,
    ad.ville,
    ad.code_postal,
    ad.latitude,
    ad.longitude
FROM agences a
LEFT JOIN adresses ad ON a.adresse_id = ad.id
ORDER BY a.created_date DESC;

-- ============================================
-- Pour supprimer l'agence de test (si besoin)
-- ============================================

-- Décommentez les lignes suivantes pour supprimer l'agence de test:
-- DELETE FROM agences WHERE code = 'LME-NYE-001';
-- DELETE FROM adresses WHERE quartier = 'Nyékonakpoè' AND ville = 'Lomé';
