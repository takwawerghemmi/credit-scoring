package com.creditscoring.service;

import com.creditscoring.dto.reponse.HistoriqueResponse;
import com.creditscoring.entity.Historique;
import com.creditscoring.repository.HistoriqueRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoriqueServiceImpl implements HistoriqueService {

    private final HistoriqueRepository historiqueRepository;

    public HistoriqueServiceImpl(HistoriqueRepository historiqueRepository) {
        this.historiqueRepository = historiqueRepository;
    }

    @Override
    public List<HistoriqueResponse> getAllHistoriques() {
        return historiqueRepository.findAll()
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    @Override
    public HistoriqueResponse getHistoriqueById(Long id) {

        Historique historique = historiqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Historique introuvable"));

        return convertir(historique);
    }

    @Override
    public List<HistoriqueResponse> getHistoriqueUtilisateur(Long utilisateurId) {
        return historiqueRepository.findByUtilisateurId(utilisateurId)
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    @Override
    public void supprimerHistorique(Long id) {
        historiqueRepository.deleteById(id);
    }

    private HistoriqueResponse convertir(Historique historique) {

        HistoriqueResponse response = new HistoriqueResponse();

        response.setId(historique.getId());
        response.setAction(historique.getAction());
        response.setDescription(historique.getDescription());
        response.setDateAction(historique.getDateAction());

        if (historique.getUtilisateur() != null) {
            response.setUtilisateurId(historique.getUtilisateur().getId());
        }

        return response;
    }
}