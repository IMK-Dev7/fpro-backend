package com.facturation.controller;

import com.facturation.dto.*;
import com.facturation.model.StatutPaiement;
import com.facturation.service.FactureService;
import com.facturation.service.PdfService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/factures")
@CrossOrigin(origins = "*")
public class FactureController {

    @Autowired
    private FactureService factureService;

    @Autowired
    private PdfService pdfService;

    @PostMapping
    public ResponseEntity<FactureResponseDTO> creerFacture(@Valid @RequestBody FactureRequestDTO requestDTO) {
        FactureResponseDTO factureDTO = factureService.creerFacture(requestDTO);
        return new ResponseEntity<>(factureDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FactureResponseDTO>> getAllFactures() {
        List<FactureResponseDTO> factures = factureService.getAllFactures();
        return ResponseEntity.ok(factures);
    }

    @GetMapping("/paginated")
    public ResponseEntity<FacturePageDTO> getAllFacturesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateFacturation") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FactureResponseDTO> facturePage = factureService.getAllFactures(pageable);

        FacturePageDTO response = new FacturePageDTO(facturePage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<FacturePageDTO> searchFactures(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<FactureResponseDTO> facturePage = factureService.searchFactures(search, pageable);

        FacturePageDTO response = new FacturePageDTO(facturePage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactureResponseDTO> getFactureById(@PathVariable Long id) {
        FactureResponseDTO factureDTO = factureService.getFactureById(id);
        return ResponseEntity.ok(factureDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FactureResponseDTO> updateFacture(
            @PathVariable Long id,
            @Valid @RequestBody FactureRequestDTO requestDTO) {
        FactureResponseDTO factureDTO = factureService.updateFacture(id, requestDTO);
        return ResponseEntity.ok(factureDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFacture(@PathVariable Long id) {
        factureService.deleteFacture(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Facture supprimée avec succès");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, String>> deleteMultipleFactures(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            try {
                factureService.deleteFacture(id);
            } catch (Exception e) {
                // Log l'erreur mais continue avec les autres
                System.err.println("Erreur lors de la suppression de la facture " + id + ": " + e.getMessage());
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", ids.size() + " facture(s) supprimée(s) avec succès");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend de facturation fonctionnel !");
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // Vous pouvez ajouter ici des statistiques
        // Par exemple: nombre total de factures, montant total, etc.

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<FactureResponseDTO> previewFacture(@PathVariable Long id) {
        try {
            FactureResponseDTO factureDTO = factureService.getFactureById(id);
            return ResponseEntity.ok(factureDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadFacturePdf(@PathVariable Long id) {
        try {
            // Récupérer la facture
            FactureResponseDTO facture = factureService.getFactureById(id);

            // Générer le PDF
            byte[] pdfBytes = pdfService.generateFacturePdf(facture);

            // Nom du fichier
            String filename = "Facture_" + facture.getNumeroFacture() + ".pdf";

            // Retourner la réponse avec le PDF
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/{id}/view", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> viewFacturePdf(@PathVariable Long id) {
        try {
            // Récupérer la facture
            FactureResponseDTO facture = factureService.getFactureById(id);

            // Générer le PDF
            byte[] pdfBytes = pdfService.generateFacturePdf(facture);

            // Nom du fichier
            String filename = "facture_" + facture.getNumeroFacture() + ".pdf";

            // Retourner la réponse avec le PDF en mode inline (visualisation)
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            // Log de l'erreur
            System.err.println("Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();

            // Retourner une erreur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erreur lors de la génération du PDF: " + e.getMessage()).getBytes());
        }
    }

    // ==============================
    // NOUVELLES FONCTIONNALITÉS DE PAIEMENT
    // ==============================

    @GetMapping("/by-statut")
    public ResponseEntity<FacturePageDTO> getFacturesByStatut(
            @RequestParam StatutPaiement statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateFacturation") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FactureResponseDTO> facturePage = factureService.getFacturesByStatut(statut, pageable);

        FacturePageDTO response = new FacturePageDTO(facturePage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reste-a-payer")
    public ResponseEntity<List<FactureResponseDTO>> getFacturesAvecResteAPayer() {
        List<FactureResponseDTO> factures = factureService.getFacturesAvecResteAPayer();
        return ResponseEntity.ok(factures);
    }

    @GetMapping("/paginated/reste-a-payer")
    public ResponseEntity<FacturePageDTO> getFacturesAvecResteAPayerPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "resteAPayer") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FactureResponseDTO> facturePage = factureService.getFacturesAvecResteAPayer(pageable);

        FacturePageDTO response = new FacturePageDTO(facturePage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats-paiements")
    public ResponseEntity<Map<String, Object>> getStatsPaiements() {
        Map<String, Object> stats = factureService.getStatsPaiements();
        return ResponseEntity.ok(stats);
    }
}