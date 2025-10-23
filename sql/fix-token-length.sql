-- Script pour augmenter la taille de la colonne token dans la table tokens
-- Le JWT peut être très long (plus de 255 caractères) quand il contient beaucoup de permissions

ALTER TABLE tokens ALTER COLUMN token TYPE TEXT;
