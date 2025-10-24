-- Script pour vérifier et configurer le rôle SUPERVISEUR
-- Utilise les utilisateurs existants dans la base de données

-- 1. Afficher tous les rôles existants
SELECT '=== RÔLES EXISTANTS ===' as info;
SELECT id, name FROM roles ORDER BY name;

-- 2. Vérifier/Créer le rôle SUPERVISEUR s'il n'existe pas
INSERT INTO roles (name, created_date, last_modified_date)
SELECT 'ROLE_SUPERVISEUR', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SUPERVISEUR');

-- 3. Afficher tous les utilisateurs existants avec leurs rôles
SELECT '=== UTILISATEURS EXISTANTS ===' as info;
SELECT 
    u.id,
    u.nom,
    u.prenom,
    u.email,
    u.enabled as actif,
    u.account_locked as verrouille,
    STRING_AGG(r.name, ', ') as roles
FROM utilisateurs u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id, u.nom, u.prenom, u.email, u.enabled, u.account_locked
ORDER BY u.id;

-- 4. Instructions pour assigner le rôle SUPERVISEUR à un utilisateur existant
-- Décommentez et modifiez l'email selon l'utilisateur choisi :

/*
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM utilisateurs u, roles r
WHERE u.email = 'email-de-lutilisateur@exemple.com'  -- MODIFIER CET EMAIL
  AND r.name = 'ROLE_SUPERVISEUR'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );
*/

-- 5. Exemple: Assigner SUPERVISEUR au premier utilisateur actif qui a le rôle OPERATEUR
-- Décommentez pour exécuter :

/*
INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT u.id, r_super.id
FROM utilisateurs u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r_op ON ur.role_id = r_op.id
CROSS JOIN roles r_super
WHERE r_op.name = 'ROLE_OPERATEUR'
  AND r_super.name = 'ROLE_SUPERVISEUR'
  AND u.enabled = true
  AND u.account_locked = false
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur2
    WHERE ur2.user_id = u.id AND ur2.role_id = r_super.id
  )
LIMIT 1;
*/
