-- ============================================================
-- Correction de la contrainte d'unicité sur l'email
-- Pour permettre la restauration d'utilisateurs supprimés
-- ============================================================

-- 1. Supprimer l'ancienne contrainte unique sur email
ALTER TABLE utilisateurs DROP CONSTRAINT IF EXISTS uk6ldvumu3hqvnmmxy1b6lsxwqy;

-- Supprimer aussi toute autre contrainte unique sur email si elle existe
ALTER TABLE utilisateurs DROP CONSTRAINT IF EXISTS utilisateurs_email_key;

-- 2. Créer un index unique partiel qui ignore les utilisateurs supprimés
-- Cet index permet d'avoir plusieurs utilisateurs avec le même email
-- si certains sont marqués comme supprimés (is_deleted = true)
CREATE UNIQUE INDEX unique_email_active_users 
ON utilisateurs(email) 
WHERE is_deleted = false;

-- Vérification
SELECT 
    conname AS constraint_name,
    contype AS constraint_type
FROM pg_constraint
WHERE conrelid = 'utilisateurs'::regclass
  AND conname LIKE '%email%';

-- Afficher les index sur la table utilisateurs
SELECT 
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'utilisateurs'
  AND indexname LIKE '%email%';

COMMENT ON INDEX unique_email_active_users IS 'Index unique partiel : un email ne peut exister qu''une seule fois parmi les utilisateurs actifs (is_deleted = false)';
