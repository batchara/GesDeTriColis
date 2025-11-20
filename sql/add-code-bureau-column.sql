-- Ajouter la colonne code_bureau à la table agences
ALTER TABLE agences ADD COLUMN code_bureau VARCHAR(50);

-- Créer un index sur code_bureau pour améliorer les performances de recherche
CREATE INDEX idx_agences_code_bureau ON agences(code_bureau);
