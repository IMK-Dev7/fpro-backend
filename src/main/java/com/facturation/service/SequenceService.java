package com.facturation.service;

import com.facturation.repository.FactureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class SequenceService {

    @Autowired
    private FactureRepository factureRepository;

    public String genererNumeroFacture() {
        LocalDate now = LocalDate.now();
        int annee = now.getYear() % 100; // Garde les 2 derniers chiffres
        int mois = now.getMonthValue();
        int jour = now.getDayOfMonth();

        String datePrefix = String.format("%02d%02d%02d", annee, mois, jour);

        // Commencer à 1 et incrémenter jusqu'à trouver un numéro disponible
        int sequence = 1;
        String numeroEssai;

        do {
            numeroEssai = String.format("%s-%03d", datePrefix, sequence);
            sequence++;
        } while (numeroExisteDeja(numeroEssai));

        return numeroEssai;
    }

    private boolean numeroExisteDeja(String numeroFacture) {
        // Vérifier si une facture avec ce numéro existe déjà
        return factureRepository.existsByNumeroFacture(numeroFacture);
    }
}