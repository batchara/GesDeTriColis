-- Script pour corriger la contrainte sur la table permissions
-- Cette contrainte empêche l'insertion de nouvelles permissions

-- Supprimer l'ancienne contrainte CHECK qui limite les valeurs
ALTER TABLE permissions DROP CONSTRAINT IF EXISTS permissions_nom_check;

-- Optionnellement, vous pouvez recréer la contrainte avec toutes les valeurs autorisées
-- Mais il est préférable de laisser Hibernate gérer les valeurs de l'enum
-- ALTER TABLE permissions ADD CONSTRAINT permissions_nom_check 
-- CHECK (nom IN ('ADMIN_READ', 'ADMIN_CREATE', 'ADMIN_UPDATE', 'ADMIN_DELETE', 'ADMIN_VIEW_DASHBOARD',
--                'SUPERVISEUR_READ', 'SUPERVISEUR_CREATE', 'SUPERVISEUR_UPDATE', 'SUPERVISEUR_DELETE', 'SUPERVISEUR_VIEW_DASHBOARD'));

-- Nettoyer les données existantes si nécessaire
-- DELETE FROM role_permissions;
-- DELETE FROM permissions;
