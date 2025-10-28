-- Script SQL pour créer la table notifications (PostgreSQL)
-- Date: 28 octobre 2025
-- Description: Table pour gérer les notifications du système (superviseur -> admin)

CREATE TABLE IF NOT EXISTS notifications (
    id SERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER NOT NULL,
    entity_name VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NON_LUE',
    action_required BOOLEAN NOT NULL DEFAULT FALSE,
    reason TEXT,
    created_by VARCHAR(255) NOT NULL,
    target_user_id VARCHAR(255) NOT NULL,
    details TEXT,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP NULL
);

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_target_user ON notifications(target_user_id);
CREATE INDEX IF NOT EXISTS idx_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_action_required ON notifications(action_required);
CREATE INDEX IF NOT EXISTS idx_created_date ON notifications(created_date);

-- Commentaires sur les colonnes
COMMENT ON TABLE notifications IS 'Table des notifications du système';
COMMENT ON COLUMN notifications.type IS 'Type de notification (CREATION, MODIFICATION, SUPPRESSION_DEMANDE, etc.)';
COMMENT ON COLUMN notifications.entity_type IS 'Type d''entité (UTILISATEUR, AGENCE, COLIS)';
COMMENT ON COLUMN notifications.entity_id IS 'ID de l''entité concernée';
COMMENT ON COLUMN notifications.entity_name IS 'Nom de l''entité (pour affichage)';
COMMENT ON COLUMN notifications.message IS 'Message de la notification';
COMMENT ON COLUMN notifications.status IS 'Statut (NON_LUE, LUE, EN_ATTENTE_VALIDATION, TRAITEE)';
COMMENT ON COLUMN notifications.action_required IS 'Nécessite une action admin (true pour suppressions)';
COMMENT ON COLUMN notifications.reason IS 'Motif de la demande ou du rejet';
COMMENT ON COLUMN notifications.created_by IS 'Email du superviseur créateur';
COMMENT ON COLUMN notifications.target_user_id IS 'Email de l''admin destinataire';
COMMENT ON COLUMN notifications.details IS 'Détails JSON des changements';

-- Exemple d'insertion de test
/*
INSERT INTO notifications (type, entity_type, entity_id, entity_name, message, status, action_required, reason, created_by, target_user_id, details)
VALUES 
    ('CREATION', 'UTILISATEUR', 1, 'Jean Dupont', 'Création d''un nouvel utilisateur', 'NON_LUE', FALSE, NULL, 'superviseur@poste.tg', 'admin@poste.tg', '{"email":"jean.dupont@poste.tg"}'),
    ('SUPPRESSION_DEMANDE', 'AGENCE', 2, 'Agence Lomé Centre', 'Demande de suppression d''agence', 'EN_ATTENTE_VALIDATION', TRUE, 'Fermeture définitive', 'superviseur@poste.tg', 'admin@poste.tg', NULL);
*/
