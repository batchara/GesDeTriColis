-- Script pour vérifier et attribuer le rôle OPERATEUR à rarabatcha@gmail.com
-- Date: 29 octobre 2025

-- 1. Vérifier l'utilisateur et ses rôles actuels
SELECT u.id, u.email, u.nom, u.prenom, u.enabled, u.account_locked,
       r.name as role
FROM utilisateurs u
LEFT JOIN user_roles ur ON u.id = ur.id_user
LEFT JOIN roles r ON ur.id_role = r.id
WHERE u.email = 'rarabatcha@gmail.com';

-- 2. Vérifier si le rôle OPERATEUR existe
SELECT * FROM roles WHERE name = 'ROLE_OPERATEUR';

-- 3. Supprimer tous les rôles existants pour cet utilisateur (si nécessaire)
-- Décommentez si vous voulez remplacer tous les rôles
-- DELETE FROM user_roles 
-- WHERE id_user = (SELECT id FROM utilisateurs WHERE email = 'rarabatcha@gmail.com');

-- 4. Attribuer le rôle OPERATEUR à rarabatcha@gmail.com
INSERT INTO user_roles (id_user, id_role)
SELECT u.id, r.id
FROM utilisateurs u, roles r
WHERE u.email = 'rarabatcha@gmail.com' 
  AND r.name = 'ROLE_OPERATEUR'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.id_user = u.id AND ur.id_role = r.id
  );

-- 5. S'assurer que le compte est activé et déverrouillé
UPDATE utilisateurs
SET enabled = true,
    account_locked = false
WHERE email = 'rarabatcha@gmail.com';

-- 6. Vérifier le résultat final
SELECT u.id, u.email, u.nom, u.prenom, u.enabled, u.account_locked,
       string_agg(r.name, ', ') as roles
FROM utilisateurs u
LEFT JOIN user_roles ur ON u.id = ur.id_user
LEFT JOIN roles r ON ur.id_role = r.id
WHERE u.email = 'rarabatcha@gmail.com'
GROUP BY u.id, u.email, u.nom, u.prenom, u.enabled, u.account_locked;
