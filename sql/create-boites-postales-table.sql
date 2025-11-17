-- Script de création de la table boites_postales
-- Une boîte postale est associée à une agence (relation ManyToOne)

CREATE TABLE IF NOT EXISTS boites_postales (
    -- Identifiant principal
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Informations de la boîte postale
    libelle VARCHAR(200) COMMENT 'Libellé ou description de la boîte postale',
    capacite INT COMMENT 'Capacité en litres ou nombre de colis',
    
    -- Relation avec l'agence
    agence_id INT NOT NULL COMMENT 'ID de l''agence à laquelle appartient la boîte postale',
    
    -- Colonnes d'audit temporel
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Date de création',
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Date de dernière modification',
    deleted_at TIMESTAMP NULL COMMENT 'Date de suppression (soft delete)',
    
    -- Colonnes d'audit utilisateur
    created_by VARCHAR(255) COMMENT 'Utilisateur qui a créé l''enregistrement',
    last_modified_by VARCHAR(255) COMMENT 'Dernier utilisateur qui a modifié l''enregistrement',
    deleted_by VARCHAR(255) COMMENT 'Utilisateur qui a supprimé l''enregistrement',
    
    -- Clé étrangère vers la table agences
    CONSTRAINT fk_boite_postale_agence FOREIGN KEY (agence_id) REFERENCES agences(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    
    -- Index pour optimiser les requêtes
    INDEX idx_boite_postale_agence (agence_id),
    INDEX idx_boite_postale_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Table des boîtes postales';
