package com.raoudate.GestionDeTri.Enum;

public enum StatutColis {
    EN_ATTENTE,       // scanné, en attente d’affectation
    AFFECTE,          // agence affectée
    EXPEDIE,          // envoyé vers l’agence
    RECEPTIONNE,      // reçu par l’agence
    LIVRE,
    ANNULEE
}