package com.facturation.dto;

import com.facturation.model.StatutPaiement;
import java.time.LocalDate;
import java.util.List;

public class FactureResponseDTO {

    private Long id;
    private String numeroFacture;
    private LocalDate dateFacturation;
    private String nomClient;
    private Double total;
    private Double montantPaye;
    private Double resteAPayer;
    private StatutPaiement statutPaiement;
    private String totalEnLettres;
    private List<LigneFactureDTO> lignes;
    private List<PaiementDTO> paiements; // Nouveau

    // Constructeurs
    public FactureResponseDTO() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroFacture() { return numeroFacture; }
    public void setNumeroFacture(String numeroFacture) { this.numeroFacture = numeroFacture; }

    public LocalDate getDateFacturation() { return dateFacturation; }
    public void setDateFacturation(LocalDate dateFacturation) { this.dateFacturation = dateFacturation; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public Double getMontantPaye() { return montantPaye; }
    public void setMontantPaye(Double montantPaye) { this.montantPaye = montantPaye; }

    public Double getResteAPayer() { return resteAPayer; }
    public void setResteAPayer(Double resteAPayer) { this.resteAPayer = resteAPayer; }

    public StatutPaiement getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(StatutPaiement statutPaiement) { this.statutPaiement = statutPaiement; }

    public String getTotalEnLettres() { return totalEnLettres; }
    public void setTotalEnLettres(String totalEnLettres) { this.totalEnLettres = totalEnLettres; }

    public List<LigneFactureDTO> getLignes() { return lignes; }
    public void setLignes(List<LigneFactureDTO> lignes) { this.lignes = lignes; }

    public List<PaiementDTO> getPaiements() { return paiements; }
    public void setPaiements(List<PaiementDTO> paiements) { this.paiements = paiements; }
}