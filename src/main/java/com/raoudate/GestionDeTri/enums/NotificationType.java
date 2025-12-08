package com.raoudate.GestionDeTri.enums;

/**
 * Type de notification
 */
public enum NotificationType {
    CREATION,                  // Création d'une entité
    MODIFICATION,              // Modification d'une entité
    MODIFICATION_DEMANDE,      // Demande de modification (nécessite validation admin)
    MODIFICATION_VALIDEE,      // Modification approuvée par l'admin
    MODIFICATION_REFUSEE,      // Modification refusée par l'admin
    SUPPRESSION_DEMANDE,       // Demande de suppression (nécessite validation admin)
    SUPPRESSION_VALIDEE,       // Suppression approuvée par l'admin
    SUPPRESSION_REFUSEE,       // Suppression refusée par l'admin
    AFFECTATION,               // Affectation d'une entité à un utilisateur
    CHANGEMENT_STATUT          // Changement de statut d'un colis
}
