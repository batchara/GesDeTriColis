#!/bin/bash

# ============================================
# Script de test complet pour les agences
# ============================================
# Ce script insère une agence de test et vérifie l'affichage
# Usage: ./test-agency-complete.sh

echo "🏢 Test complet de gestion des agences"
echo "========================================"
echo ""

# Configuration de la base de données
DB_NAME="gesco"
DB_USER="postgres"
DB_HOST="localhost"
DB_PORT="5432"

echo "📊 Étape 1: Vérification de la structure de la table..."
psql -U $DB_USER -d $DB_NAME -c "
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'agences'
ORDER BY ordinal_position;
"

echo ""
echo "📝 Étape 2: Insertion d'une agence de test..."
psql -U $DB_USER -d $DB_NAME << 'EOF'
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

    -- Insérer l'agence
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

    RAISE NOTICE '✅ Agence créée avec succès (Adresse ID: %)', v_adresse_id;
END $$;
EOF

echo ""
echo "🔍 Étape 3: Affichage de toutes les agences..."
psql -U $DB_USER -d $DB_NAME -c "
SELECT 
    a.id,
    a.label AS nom,
    a.code,
    a.email,
    a.num_tel,
    a.region,
    ad.ville,
    ad.quartier
FROM agences a
LEFT JOIN adresses ad ON a.adresse_id = ad.id
ORDER BY a.created_date DESC;
"

echo ""
echo "📊 Étape 4: Statistiques par région..."
psql -U $DB_USER -d $DB_NAME -c "
SELECT 
    region,
    COUNT(*) AS nombre_agences
FROM agences
GROUP BY region
ORDER BY nombre_agences DESC;
"

echo ""
echo "🌐 Étape 5: Test API - Format JSON (similaire au backend)..."
psql -U $DB_USER -d $DB_NAME -c "
SELECT json_build_object(
    'id', a.id,
    'nom', a.label,
    'code', a.code,
    'email', a.email,
    'numTel', a.num_tel,
    'region', a.region,
    'adresse', json_build_object(
        'adresseComplete', ad.adresse_complete,
        'quartier', ad.quartier,
        'ville', ad.ville,
        'codePostale', ad.code_postal
    )
) AS agence_json
FROM agences a
LEFT JOIN adresses ad ON a.adresse_id = ad.id
WHERE a.code = 'LME-NYE-001';
"

echo ""
echo "========================================"
echo "✅ Test terminé !"
echo ""
echo "📝 Prochaines étapes:"
echo "  1. Démarrer le backend: cd TriCoBack && ./mvnw spring-boot:run"
echo "  2. Vérifier l'API: curl http://localhost:8081/api/v1/agences"
echo "  3. Ouvrir l'interface admin: http://localhost:4200/admin"
echo "  4. Ouvrir l'interface superviseur: http://localhost:4200/superviseur"
echo ""
echo "🗑️  Pour supprimer l'agence de test:"
echo "  psql -U postgres -d gesco -c \"DELETE FROM agences WHERE code = 'LME-NYE-001';\""
echo ""
