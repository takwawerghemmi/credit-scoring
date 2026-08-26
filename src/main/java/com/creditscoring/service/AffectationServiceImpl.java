package com.creditscoring.service;

import com.creditscoring.entity.Conseiller;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.repository.ConseillerRepository;
import com.creditscoring.repository.DemandeCreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AffectationServiceImpl implements AffectationService {

    private final DemandeCreditRepository demandeCreditRepository;
    private final ConseillerRepository conseillerRepository;

    @Override
    public String affecterDemande(Long demandeId, Long conseillerId) {

        DemandeCredit demande = demandeCreditRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        Conseiller conseiller = conseillerRepository.findById(conseillerId)
                .orElseThrow(() -> new RuntimeException("Conseiller introuvable"));

        demande.setConseiller(conseiller);

        demandeCreditRepository.save(demande);

        return "La demande " + demandeId +
                " a été affectée au conseiller " +
                conseiller.getNom() + " " + conseiller.getPrenom();
    }
}