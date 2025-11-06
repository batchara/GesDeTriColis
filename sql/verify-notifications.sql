-- Script de vérification du système de notifications
-- Exécuter dans MySQL pour vérifier l'état des notifications

-- 1. Voir toutes les notifications avec leur statut
SELECT 
    id,
    type,
    entity_type,
    entity_name,
    status,
    action_required,
    reason,
    created_by,
    target_user_id,
    DATE_FORMAT(date_creation, '%Y-%m-%d %H:%i:%s') as date_creation
FROM notifications
ORDER BY date_creation DESC
LIMIT 20;

-- 2. Compter les notifications par statut
SELECT 
    status,
    COUNT(*) as count
FROM notifications
GROUP BY status;

-- 3. Voir spécifiquement les notifications TRAITEE
SELECT 
    id,
    type,
    entity_type,
    entity_name,
    status,
    action_required,
    reason,
    created_by,
    target_user_id,
    message,
    DATE_FORMAT(date_creation, '%Y-%m-%d %H:%i:%s') as date_creation
FROM notifications
WHERE status = 'TRAITEE'
ORDER BY date_creation DESC;

-- 4. Compter les notifications par utilisateur et statut
SELECT 
    target_user_id,
    status,
    COUNT(*) as count
FROM notifications
GROUP BY target_user_id, status
ORDER BY target_user_id, status;

-- 5. Voir les notifications avec actionRequired
SELECT 
    id,
    type,
    entity_name,
    status,
    action_required,
    reason,
    target_user_id,
    DATE_FORMAT(date_creation, '%Y-%m-%d %H:%i:%s') as date_creation
FROM notifications
WHERE action_required = TRUE
ORDER BY date_creation DESC;

-- 6. Vérifier s'il y a des notifications récentes (dernières 24h)
SELECT 
    id,
    type,
    status,
    action_required,
    reason,
    message,
    target_user_id,
    DATE_FORMAT(date_creation, '%Y-%m-%d %H:%i:%s') as date_creation
FROM notifications
WHERE date_creation >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY date_creation DESC;

-- 7. Trouver une notification spécifique par ID (remplacer X par l'ID)
-- SELECT * FROM notifications WHERE id = X;

-- 8. Mettre à jour manuellement une notification pour test (si nécessaire)
-- UPDATE notifications SET status = 'TRAITEE', action_required = FALSE, reason = 'Test manuel' WHERE id = X;
