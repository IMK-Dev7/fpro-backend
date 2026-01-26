package com.facturation.repository;

import com.facturation.model.Facture;
import com.facturation.model.StatutPaiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    // Recherche par nom client ou numéro de facture
    Page<Facture> findByNomClientContainingIgnoreCaseOrNumeroFactureContainingIgnoreCase(
            String nomClient, String numeroFacture, Pageable pageable);

    // Recherche avancée avec JPQL
    @Query("SELECT f FROM Facture f WHERE " +
            "LOWER(f.nomClient) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(f.numeroFacture) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Facture> searchFactures(@Param("search") String search, Pageable pageable);

    // Compter le nombre de factures par mois
    @Query("SELECT MONTH(f.dateFacturation) as mois, COUNT(f) as nombre " +
            "FROM Facture f " +
            "WHERE YEAR(f.dateFacturation) = :annee " +
            "GROUP BY MONTH(f.dateFacturation)")
    List<Object[]> countByMonth(@Param("annee") int annee);

    // Méthodes pour le suivi des paiements
    Page<Facture> findByStatutPaiement(StatutPaiement statut, Pageable pageable);

    List<Facture> findByResteAPayerGreaterThan(Double montant);

    // Pour une meilleure pagination des factures avec reste à payer
    Page<Facture> findByResteAPayerGreaterThan(Double montant, Pageable pageable);

    // Statistiques
    @Query("SELECT SUM(f.total) FROM Facture f")
    Double findTotalFactures();

    @Query("SELECT SUM(f.montantPaye) FROM Facture f")
    Double findTotalPaiements();

    @Query("SELECT SUM(f.resteAPayer) FROM Facture f")
    Double findTotalResteAPayer();

    // Comptage par statut
    @Query("SELECT COUNT(f) FROM Facture f WHERE f.statutPaiement = :statut")
    Long countByStatutPaiement(@Param("statut") StatutPaiement statut);

    boolean existsByNumeroFacture(String numeroFacture);
}