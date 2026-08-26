package com.creditscoring.service.impl;

import com.creditscoring.dto.request.BanqueRequest;
import com.creditscoring.dto.reponse.BanqueResponse;
import com.creditscoring.entity.Banque;
import com.creditscoring.mapper.BanqueMapper;
import com.creditscoring.repository.BanqueRepository;
import com.creditscoring.service.BanqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BanqueServiceImpl implements BanqueService {

    private final BanqueRepository banqueRepository;

    @Override
    public BanqueResponse ajouter(BanqueRequest request){

        Banque banque= BanqueMapper.toEntity(request);

        banqueRepository.save(banque);

        return BanqueMapper.toResponse(banque);

    }

    @Override
    public BanqueResponse modifier(Long id,BanqueRequest request){

        Banque banque=banqueRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Banque introuvable"));

        banque.setNom(request.getNom());
        banque.setCodeBanque(request.getCodeBanque());
        banque.setAdresse(request.getAdresse());
        banque.setTelephone(request.getTelephone());
        banque.setEmail(request.getEmail());
        banque.setSiteWeb(request.getSiteWeb());

        banqueRepository.save(banque);

        return BanqueMapper.toResponse(banque);

    }

    @Override
    public BanqueResponse trouverParId(Long id){

        Banque banque=banqueRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Banque introuvable"));

        return BanqueMapper.toResponse(banque);

    }

    @Override
    public List<BanqueResponse> afficherToutes(){

        return banqueRepository.findAll()
                .stream()
                .map(BanqueMapper::toResponse)
                .collect(Collectors.toList());

    }

    @Override
    public void supprimer(Long id){

        banqueRepository.deleteById(id);

    }

}