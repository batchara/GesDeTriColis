-- Script SQL pour assigner les rôles aux utilisateurs existants
-- À exécuter dans votre base de données PostgreSQL

-- 1. Vérifier les utilisateurs sans rôles
SELECT u.id, u.email, u.nom, u.prenom 
FROM _user u
LEFT JOIN user_roles ur ON u.id = ur.id_user
WHERE ur.id_role IS NULL;

-- 2. Assigner le rôle ADMIN à rarabatcha@gmail.com
INSERT INTO user_roles (id_user, id_role)
SELECT u.id, r.id
FROM _user u, roles r
WHERE u.email = 'rarabatcha@gmail.com' 
  AND r.name = 'ROLE_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.id_user = u.id AND ur.id_role = r.id
  );

-- 3. Assigner un rôle à assash022001@gmail.com (à adapter selon le rôle souhaité)
-- Option A : ADMIN
INSERT INTO user_roles (id_user, id_role)
SELECT u.id, r.id
FROM _user u, roles r
WHERE u.email = 'assash022001@gmail.com' 
  AND r.name = 'ROLE_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.id_user = u.id AND ur.id_role = r.id
  );

-- Option B : SUPERVISEUR
-- INSERT INTO user_roles (id_user, id_role)
-- SELECT u.id, r.id
-- FROM _user u, roles r
-- WHERE u.email = 'assash022001@gmail.com' 
--   AND r.name = 'ROLE_SUPERVISEUR'
--   AND NOT EXISTS (
--     SELECT 1 FROM user_roles ur 
--     WHERE ur.id_user = u.id AND ur.id_role = r.id
--   );

-- Option C : OPERATEUR
-- INSERT INTO user_roles (id_user, id_role)
-- SELECT u.id, r.id
-- FROM _user u, roles r
-- WHERE u.email = 'assash022001@gmail.com' 
--   AND r.name = 'ROLE_OPERATEUR'
--   AND NOT EXISTS (
--     SELECT 1 FROM user_roles ur 
--     WHERE ur.id_user = u.id AND ur.id_role = r.id
--   );

-- 4. Vérifier les rôles assignés
SELECT u.email, r.name as role
FROM _user u
INNER JOIN user_roles ur ON u.id = ur.id_user
INNER JOIN roles r ON ur.id_role = r.id
WHERE u.email IN ('rarabatcha@gmail.com', 'assash022001@gmail.com')
ORDER BY u.email;

-- 5. (Optionnel) Supprimer l'ancienne colonne role de la table _user
-- ATTENTION : Sauvegardez votre base avant d'exécuter cette commande !
-- ALTER TABLE _user DROP COLUMN IF EXISTS role;
