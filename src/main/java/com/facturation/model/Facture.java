package com.facturation.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factures")
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_facture", unique = true, nullable = false)
    private String numeroFacture;

    @Column(name = "date_facturation", nullable = false)
    private LocalDate dateFacturation;

    @Column(name = "nom_client", nullable = false)
    private String nomClient;

    @Column(name = "total", nullable = false)
    private Double total = 0.0;

    @Column(name = "montant_paye")
    private Double montantPaye = 0.0;

    @Column(name = "reste_a_payer")
    private Double resteAPayer = 0.0;

    @Column(name = "statut_paiement")
    @Enumerated(EnumType.STRING)
    private StatutPaiement statutPaiement = StatutPaiement.IMPAYEE;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneFacture> lignes = new ArrayList<>();

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Paiement> paiements = new ArrayList<>();

    // Constructeurs
    public Facture() {
        this.dateFacturation = LocalDate.now();
        this.montantPaye = 0.0;
        this.resteAPayer = this.total;
        this.statutPaiement = StatutPaiement.IMPAYEE;
    }

    public Facture(String nomClient) {
        this();
        this.nomClient = nomClient;
    }

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
    public void setTotal(Double total) {
        this.total = total;
        calculerResteAPayer();
    }

    public Double getMontantPaye() { return montantPaye; }
    public void setMontantPaye(Double montantPaye) {
        this.montantPaye = montantPaye;
        calculerResteAPayer();
    }

    public Double getResteAPayer() { return resteAPayer; }
    public void setResteAPayer(Double resteAPayer) { this.resteAPayer = resteAPayer; }

    public StatutPaiement getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(StatutPaiement statutPaiement) { this.statutPaiement = statutPaiement; }

    public List<LigneFacture> getLignes() { return lignes; }
    public void setLignes(List<LigneFacture> lignes) { this.lignes = lignes; }

    public List<Paiement> getPaiements() { return paiements; }
    public void setPaiements(List<Paiement> paiements) { this.paiements = paiements; }

    // Méthodes utilitaires
    public void addLigne(LigneFacture ligne) {
        lignes.add(ligne);
        ligne.setFacture(this);
        calculerTotal();
    }

    public void addPaiement(Paiement paiement) {
        paiements.add(paiement);
        paiement.setFacture(this);
        this.montantPaye += paiement.getMontant();
        calculerResteAPayer();
    }

    public void calculerTotal() {
        this.total = lignes.stream()
                .mapToDouble(LigneFacture::getMontant)
                .sum();
        calculerResteAPayer();
    }

    private void calculerResteAPayer() {
        if (this.total == null) {
            this.total = 0.0;
        }
        if (this.montantPaye == null) {
            this.montantPaye = 0.0;
        }
        this.resteAPayer = this.total - this.montantPaye;

        if (this.resteAPayer < 0) {
            this.resteAPayer = 0.0;
        }

        // Mettre à jour le statut
        this.statutPaiement = StatutPaiement.getByMontant(this.total, this.montantPaye);
    }

    public void recalculerStatut() {
        Double totalPaye = paiements.stream()
                .mapToDouble(Paiement::getMontant)
                .sum();
        this.montantPaye = totalPaye;
        calculerResteAPayer();
    }
}