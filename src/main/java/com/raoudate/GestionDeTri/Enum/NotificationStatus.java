package com.raoudate.GestionDeTri.Enum;

/**
 * Statut d'une notification
 */
public enum NotificationStatus {
    NON_LUE,                // Notification non lue
    LUE,                    // Notification lue
    EN_ATTENTE_VALIDATION,  // En attente de validation (pour demandes de suppression)
    AFFECTE_VALIDATION,     // Notification avec action requise (demande de validation)
    TRAITEE                 // Notification traitée (approuvée ou refusée)
}
