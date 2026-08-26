package com.creditscoring.service;

import com.creditscoring.dto.request.AgenceRequest;
import com.creditscoring.dto.reponse.AgenceResponse;
import com.creditscoring.entity.Agence;
import com.creditscoring.entity.Banque;
import com.creditscoring.repository.AgenceRepository;
import com.creditscoring.repository.BanqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgenceServiceImpl implements AgenceService {

    private final AgenceRepository agenceRepository;
    private final BanqueRepository banqueRepository;

    @Override
    public AgenceResponse creerAgence(AgenceRequest request) {

        Banque banque = banqueRepository.findById(request.getBanqueId())
                .orElseThrow(() -> new RuntimeException("Banque introuvable"));

        Agence agence = Agence.builder()
                .nom(request.getNom())
                .codeAgence(request.getCodeAgence())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .banque(banque)
                .build();

        agenceRepository.save(agence);

        return AgenceResponse.builder()
                .id(agence.getId())
                .nom(agence.getNom())
                .codeAgence(agence.getCodeAgence())
                .adresse(agence.getAdresse())
                .telephone(agence.getTelephone())
                .banqueId(banque.getId())
                .banqueNom(banque.getNom())
                .build();
    }

    @Override
    public List<AgenceResponse> obtenirToutesLesAgences() {

        return agenceRepository.findAll()
                .stream()
                .map(a -> AgenceResponse.builder()
                        .id(a.getId())
                        .nom(a.getNom())
                        .codeAgence(a.getCodeAgence())
                        .adresse(a.getAdresse())
                        .telephone(a.getTelephone())
                        .banqueId(a.getBanque().getId())
                        .banqueNom(a.getBanque().getNom())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public AgenceResponse obtenirAgence(Long id) {

        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agence introuvable"));

        return AgenceResponse.builder()
                .id(agence.getId())
                .nom(agence.getNom())
                .codeAgence(agence.getCodeAgence())
                .adresse(agence.getAdresse())
                .telephone(agence.getTelephone())
                .banqueId(agence.getBanque().getId())
                .banqueNom(agence.getBanque().getNom())
                .build();
    }

    @Override
    public AgenceResponse modifierAgence(Long id, AgenceRequest request) {

        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agence introuvable"));

        Banque banque = banqueRepository.findById(request.getBanqueId())
                .orElseThrow(() -> new RuntimeException("Banque introuvable"));

        agence.setNom(request.getNom());
        agence.setCodeAgence(request.getCodeAgence());
        agence.setAdresse(request.getAdresse());
        agence.setTelephone(request.getTelephone());
        agence.setBanque(banque);

        agenceRepository.save(agence);

        return obtenirAgence(id);
    }

    @Override
    public void supprimerAgence(Long id) {
        agenceRepository.deleteById(id);
    }
}