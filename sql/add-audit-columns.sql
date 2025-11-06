-- Script SQL pour ajouter les colonnes d'audit à toutes les tables
-- Ce script ajoute les colonnes de traçabilité pour l'audit automatique

-- ========================================
-- AJOUT DES COLONNES D'AUDIT - Table utilisateurs
-- ========================================
ALTER TABLE utilisateurs
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE NOT NULL;

-- ========================================
-- AJOUT DES COLONNES D'AUDIT - Table agences
-- ========================================
ALTER TABLE agences
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE NOT NULL;

-- ========================================
-- AJOUT DES COLONNES D'AUDIT - Table colis
-- ========================================
ALTER TABLE colis
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE NOT NULL;

-- ========================================
-- AJOUT DES COLONNES D'AUDIT - Table roles
-- ========================================
ALTER TABLE roles
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE NOT NULL;

-- ========================================
-- AJOUT DES COLONNES D'AUDIT - Table permissions
-- ========================================
ALTER TABLE permissions
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE NOT NULL;

-- ========================================
-- AJOUT DES COLONNES D'AUDIT - Table notifications
-- ========================================
ALTER TABLE notifications
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE NOT NULL;

-- ========================================
-- AJOUT DES COLONNES D'AUDIT - Table centre_de_tri
-- ========================================
ALTER TABLE centre_de_tri
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE NOT NULL;

-- ========================================
-- INDEX pour optimiser les requêtes sur is_deleted
-- ========================================
CREATE INDEX IF NOT EXISTS idx_utilisateurs_is_deleted ON utilisateurs(is_deleted);
CREATE INDEX IF NOT EXISTS idx_agences_is_deleted ON agences(is_deleted);
CREATE INDEX IF NOT EXISTS idx_colis_is_deleted ON colis(is_deleted);
CREATE INDEX IF NOT EXISTS idx_roles_is_deleted ON roles(is_deleted);
CREATE INDEX IF NOT EXISTS idx_permissions_is_deleted ON permissions(is_deleted);
CREATE INDEX IF NOT EXISTS idx_notifications_is_deleted ON notifications(is_deleted);
CREATE INDEX IF NOT EXISTS idx_centre_de_tri_is_deleted ON centre_de_tri(is_deleted);

-- ========================================
-- VERIFICATION
-- ========================================
-- Vérifier que les colonnes ont été ajoutées correctement

SELECT 
    'utilisateurs' as table_name,
    COUNT(*) FILTER (WHERE column_name IN ('created_by', 'last_modified_by', 'deleted_at', 'deleted_by', 'is_deleted')) as audit_columns_count
FROM information_schema.columns 
WHERE table_name = 'utilisateurs'
UNION ALL
SELECT 
    'agences' as table_name,
    COUNT(*) FILTER (WHERE column_name IN ('created_by', 'last_modified_by', 'deleted_at', 'deleted_by', 'is_deleted')) as audit_columns_count
FROM information_schema.columns 
WHERE table_name = 'agences'
UNION ALL
SELECT 
    'colis' as table_name,
    COUNT(*) FILTER (WHERE column_name IN ('created_by', 'last_modified_by', 'deleted_at', 'deleted_by', 'is_deleted')) as audit_columns_count
FROM information_schema.columns 
WHERE table_name = 'colis';

-- Note: Chaque table devrait avoir 5 colonnes d'audit
-- Si le compte est inférieur à 5, certaines colonnes n'ont pas été créées
