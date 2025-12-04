-- Vérifier les 5 dernières notifications
SELECT id, type, entity_name, created_by, target_user_id, status, action_required, created_date 
FROM notifications 
ORDER BY created_date DESC 
LIMIT 5;
