package com.facturation.service;

import com.facturation.model.Facture;
import com.facturation.model.LigneFacture;
import com.facturation.model.StatutPaiement;
import com.facturation.dto.*;
import com.facturation.repository.FactureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FactureService {

    @Autowired
    private FactureRepository factureRepository;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private MontantEnLettresService montantEnLettresService;

    @Autowired
    private PaiementService paiementService;

    @Transactional
    public FactureResponseDTO creerFacture(FactureRequestDTO requestDTO) {
        // Créer la facture
        Facture facture = new Facture();
        facture.setNomClient(requestDTO.getNomClient());
        facture.setNumeroFacture(sequenceService.genererNumeroFacture());

        // Ajouter les lignes
        if (requestDTO.getLignes() != null) {
            for (LigneFactureDTO ligneDTO : requestDTO.getLignes()) {
                LigneFacture ligne = new LigneFacture();
                ligne.setQuantite(ligneDTO.getQuantite());
                ligne.setDesignation(ligneDTO.getDesignation());
                ligne.setPrixUnitaire(ligneDTO.getPrixUnitaire());
                facture.addLigne(ligne);
            }
        }

        // Calculer le total
        facture.calculerTotal();

        // Sauvegarder
        facture = factureRepository.save(facture);

        return convertirEnDTO(facture);
    }

    @Transactional(readOnly = true)
    public List<FactureResponseDTO> getAllFactures() {
        return factureRepository.findAll().stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<FactureResponseDTO> getAllFactures(Pageable pageable) {
        return factureRepository.findAll(pageable)
                .map(this::convertirEnDTO);
    }

    @Transactional(readOnly = true)
    public FactureResponseDTO getFactureById(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + id));
        return convertirEnDTO(facture);
    }

    @Transactional
    public FactureResponseDTO updateFacture(Long id, FactureRequestDTO requestDTO) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + id));

        // Mettre à jour les champs
        facture.setNomClient(requestDTO.getNomClient());

        // Supprimer les anciennes lignes
        facture.getLignes().clear();

        // Ajouter les nouvelles lignes
        if (requestDTO.getLignes() != null) {
            for (LigneFactureDTO ligneDTO : requestDTO.getLignes()) {
                LigneFacture ligne = new LigneFacture();
                ligne.setQuantite(ligneDTO.getQuantite());
                ligne.setDesignation(ligneDTO.getDesignation());
                ligne.setPrixUnitaire(ligneDTO.getPrixUnitaire());
                facture.addLigne(ligne);
            }
        }

        // Recalculer le total
        facture.calculerTotal();

        // Sauvegarder
        facture = factureRepository.save(facture);

        return convertirEnDTO(facture);
    }

    @Transactional
    public void deleteFacture(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + id));

        factureRepository.delete(facture);
    }

    @Transactional(readOnly = true)
    public Page<FactureResponseDTO> searchFactures(String searchTerm, Pageable pageable) {
        return factureRepository.findByNomClientContainingIgnoreCaseOrNumeroFactureContainingIgnoreCase(
                        searchTerm, searchTerm, pageable)
                .map(this::convertirEnDTO);
    }

    // ==============================
    // NOUVELLES MÉTHODES DE PAIEMENT
    // ==============================

    @Transactional(readOnly = true)
    public Page<FactureResponseDTO> getFacturesByStatut(StatutPaiement statut, Pageable pageable) {
        return factureRepository.findByStatutPaiement(statut, pageable)
                .map(this::convertirEnDTO);
    }

    @Transactional(readOnly = true)
    public List<FactureResponseDTO> getFacturesAvecResteAPayer() {
        return factureRepository.findByResteAPayerGreaterThan(0.0).stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<FactureResponseDTO> getFacturesAvecResteAPayer(Pageable pageable) {
        List<Facture> factures = factureRepository.findByResteAPayerGreaterThan(0.0);

        // Implémentation simple de pagination manuelle
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), factures.size());

        List<FactureResponseDTO> content = factures.subList(start, end).stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                content, pageable, factures.size());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatsPaiements() {
        Map<String, Object> stats = new HashMap<>();

        // Total des factures
        List<Facture> factures = factureRepository.findAll();

        // Gérer les valeurs null
        Double totalFactures = factures.stream()
                .map(Facture::getTotal)
                .filter(Objects::nonNull)  // Filtrer les null
                .mapToDouble(Double::doubleValue)
                .sum();

        // Total des paiements
        Double totalPaiements = factures.stream()
                .map(Facture::getMontantPaye)
                .filter(Objects::nonNull)  // Filtrer les null
                .mapToDouble(Double::doubleValue)
                .sum();

        // Total des restes à payer
        Double totalResteAPayer = factures.stream()
                .map(Facture::getResteAPayer)
                .filter(Objects::nonNull)  // Filtrer les null
                .mapToDouble(Double::doubleValue)
                .sum();

        // Nombre par statut avec vérification null
        long impayees = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.IMPAYEE)
                .count();

        long partiellementPayees = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PARTIELLEMENT_PAYEE)
                .count();

        long payees = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .count();

        // Mettre à zéro si null
        stats.put("totalFactures", totalFactures != null ? totalFactures : 0.0);
        stats.put("totalPaiements", totalPaiements != null ? totalPaiements : 0.0);
        stats.put("totalResteAPayer", totalResteAPayer != null ? totalResteAPayer : 0.0);
        stats.put("nombreFactures", factures.size());
        stats.put("impayees", impayees);
        stats.put("partiellementPayees", partiellementPayees);
        stats.put("payees", payees);

        return stats;
    }

    @Transactional(readOnly = true)
    private FactureResponseDTO convertirEnDTO(Facture facture) {
        FactureResponseDTO dto = new FactureResponseDTO();
        dto.setId(facture.getId());
        dto.setNumeroFacture(facture.getNumeroFacture());
        dto.setDateFacturation(facture.getDateFacturation());
        dto.setNomClient(facture.getNomClient());
        dto.setTotal(facture.getTotal());
        dto.setMontantPaye(facture.getMontantPaye());
        dto.setResteAPayer(facture.getResteAPayer());
        dto.setStatutPaiement(facture.getStatutPaiement());

        // Convertir le total en lettres seulement
        if (facture.getTotal() != null) {
            String totalEnLettres = montantEnLettresService.convertir(facture.getTotal());
            dto.setTotalEnLettres(totalEnLettres);
        }

        // Convertir les lignes
        List<LigneFactureDTO> lignesDTO = facture.getLignes().stream()
                .map(ligne -> {
                    LigneFactureDTO ligneDTO = new LigneFactureDTO();
                    ligneDTO.setId(ligne.getId());
                    ligneDTO.setQuantite(ligne.getQuantite());
                    ligneDTO.setDesignation(ligne.getDesignation());
                    ligneDTO.setPrixUnitaire(ligne.getPrixUnitaire());
                    return ligneDTO;
                })
                .collect(Collectors.toList());

        dto.setLignes(lignesDTO);

        // Convertir les paiements (simplifié)
        List<PaiementDTO> paiementsDTO = facture.getPaiements().stream()
                .map(paiement -> {
                    PaiementDTO paiementDTO = new PaiementDTO();
                    paiementDTO.setId(paiement.getId());
                    paiementDTO.setMontant(paiement.getMontant());
                    paiementDTO.setDatePaiement(paiement.getDatePaiement());
                    return paiementDTO;
                })
                .collect(Collectors.toList());

        dto.setPaiements(paiementsDTO);

        return dto;
    }
}