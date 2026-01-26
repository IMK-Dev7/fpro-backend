package com.facturation.controller;

import com.facturation.dto.PaiementDTO;
import com.facturation.service.PaiementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/factures/{factureId}/paiements")
public class PaiementController {

    @Autowired
    private PaiementService paiementService;

    @PostMapping
    public ResponseEntity<PaiementDTO> ajouterPaiement(
            @PathVariable Long factureId,
            @Valid @RequestBody PaiementDTO paiementDTO) {
        PaiementDTO result = paiementService.ajouterPaiement(factureId, paiementDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PaiementDTO>> getPaiementsByFacture(@PathVariable Long factureId) {
        List<PaiementDTO> paiements = paiementService.getPaiementsByFactureId(factureId);
        return ResponseEntity.ok(paiements);
    }

    @DeleteMapping("/{paiementId}")
    public ResponseEntity<Map<String, String>> supprimerPaiement(
            @PathVariable Long factureId,
            @PathVariable Long paiementId) {
        paiementService.supprimerPaiement(paiementId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Paiement supprimé avec succès");
        response.put("paiementId", paiementId.toString());
        response.put("factureId", factureId.toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Double>> getTotalPaiements(@PathVariable Long factureId) {
        Double total = paiementService.getTotalPaiementsByFactureId(factureId);
        Map<String, Double> response = new HashMap<>();
        response.put("totalPaiements", total);
        return ResponseEntity.ok(response);
    }
}