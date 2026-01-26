package com.facturation.service;

import com.facturation.dto.PaiementDTO;
import com.facturation.model.Facture;
import com.facturation.model.Paiement;
import com.facturation.repository.FactureRepository;
import com.facturation.repository.PaiementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaiementService {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private FactureRepository factureRepository;

    @Transactional
    public PaiementDTO ajouterPaiement(Long factureId, PaiementDTO paiementDTO) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + factureId));

        // Vérifier que le paiement ne dépasse pas le reste à payer
        Double resteAPayer = facture.getResteAPayer();
        if (paiementDTO.getMontant() > resteAPayer) {
            throw new RuntimeException("Le montant du paiement (" + paiementDTO.getMontant() +
                    ") dépasse le reste à payer (" + resteAPayer + ")");
        }

        Paiement paiement = new Paiement();
        paiement.setMontant(paiementDTO.getMontant());
        paiement.setDatePaiement(paiementDTO.getDatePaiement());
        paiement.setFacture(facture);

        paiement = paiementRepository.save(paiement);

        // Mettre à jour la facture
        facture.addPaiement(paiement);
        factureRepository.save(facture);

        return convertirEnDTO(paiement);
    }

    @Transactional
    public void supprimerPaiement(Long paiementId) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec l'ID: " + paiementId));

        Facture facture = paiement.getFacture();

        // Supprimer le paiement
        paiementRepository.delete(paiement);

        // Recalculer le statut de la facture
        facture.recalculerStatut();
        factureRepository.save(facture);
    }

    @Transactional(readOnly = true)
    public List<PaiementDTO> getPaiementsByFactureId(Long factureId) {
        return paiementRepository.findByFactureId(factureId).stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double getTotalPaiementsByFactureId(Long factureId) {
        Double total = paiementRepository.findTotalPaiementsByFactureId(factureId);
        return total != null ? total : 0.0;
    }

    private PaiementDTO convertirEnDTO(Paiement paiement) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(paiement.getId());
        dto.setMontant(paiement.getMontant());
        dto.setDatePaiement(paiement.getDatePaiement());
        return dto;
    }
}