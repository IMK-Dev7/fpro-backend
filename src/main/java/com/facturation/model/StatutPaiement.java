package com.facturation.model;

public enum StatutPaiement {
    IMPAYEE("Impayée"),
    PARTIELLEMENT_PAYEE("Partiellement payée"),
    PAYEE("Payée");

    private final String libelle;

    StatutPaiement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    public static StatutPaiement getByMontant(Double total, Double paye) {
        if (paye == null || paye == 0) {
            return IMPAYEE;
        } else if (paye >= total) {
            return PAYEE;
        } else {
            return PARTIELLEMENT_PAYEE;
        }
    }
}