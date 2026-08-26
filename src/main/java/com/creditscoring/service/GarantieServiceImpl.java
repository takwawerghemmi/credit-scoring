package com.creditscoring.service;

import com.creditscoring.dto.request.GarantieRequest;
import com.creditscoring.dto.reponse.GarantieResponse;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Garantie;
import com.creditscoring.mapper.GarantieMapper;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.GarantieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GarantieServiceImpl implements GarantieService {

    private final GarantieRepository garantieRepository;
    private final DemandeCreditRepository demandeRepository;

    @Override
    public GarantieResponse ajouter(GarantieRequest request) {

        DemandeCredit demande = demandeRepository.findById(request.getDemandeCreditId())
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        Garantie garantie = Garantie.builder()
                .type(request.getType())
                .valeur(request.getValeur())
                .description(request.getDescription())
                .demandeCredit(demande)
                .build();

        garantieRepository.save(garantie);

        return GarantieMapper.toResponse(garantie);
    }

    @Override
    public List<GarantieResponse> afficherToutes() {

        return garantieRepository.findAll()
                .stream()
                .map(GarantieMapper::toResponse)
                .toList();
    }
    @Override
    public List<GarantieResponse> afficherParDemande(
            Long demandeId
    ) {

        return garantieRepository
                .findByDemandeCreditId(demandeId)
                .stream()
                .map(GarantieMapper::toResponse)
                .toList();
    }
    @Override
    public void supprimer(Long id) {
        garantieRepository.deleteById(id);
    }
}