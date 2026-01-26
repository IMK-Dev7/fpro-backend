package com.facturation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class PaiementDTO {

    private Long id;

    @NotNull(message = "Le montant est obligatoire")
    @Min(value = 1, message = "Le montant doit être au moins 1")
    private Double montant;

    private LocalDate datePaiement;

    // Constructeurs
    public PaiementDTO() {
        this.datePaiement = LocalDate.now();
    }

    public PaiementDTO(Double montant) {
        this();
        this.montant = montant;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public LocalDate getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDate datePaiement) { this.datePaiement = datePaiement; }
}