package com.raoudate.GestionDeTri.Enum;

public enum StatutColis {
    EN_ATTENTE,       // en attente de traitement
    AFFECTE,          // agence affectée automatiquement après OCR + géocodage
    EXPEDIE,          // envoyé vers l'agence
    RECEPTIONNE,      // reçu par l'agence
    LIVRE,            // livré au destinataire
    RETOUR            // retour
}