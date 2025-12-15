-- Ajout de la colonne last_login à la table utilisateurs
-- Cette colonne permet de tracer la dernière connexion réussie de chaque utilisateur

ALTER TABLE utilisateurs 
ADD COLUMN IF NOT EXISTS last_login DATE;

-- Commentaire sur la colonne
COMMENT ON COLUMN utilisateurs.last_login IS 'Date de la dernière connexion réussie de l''utilisateur';
