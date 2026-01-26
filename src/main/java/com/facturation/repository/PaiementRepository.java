package com.facturation.repository;

import com.facturation.model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findByFactureId(Long factureId);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.facture.id = :factureId")
    Double findTotalPaiementsByFactureId(@Param("factureId") Long factureId);
}