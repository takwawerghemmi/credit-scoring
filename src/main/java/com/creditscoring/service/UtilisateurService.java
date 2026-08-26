package com.creditscoring.service;

import com.creditscoring.dto.request.BanqueRequest;
import com.creditscoring.dto.request.UpdateUtilisateurRequest;
import com.creditscoring.dto.reponse.UtilisateurResponse;
import com.creditscoring.entity.Utilisateur;
import java.util.List;

public interface UtilisateurService {

    UtilisateurResponse creerClient(BanqueRequest.RegisterClientRequest request);

    UtilisateurResponse obtenirUtilisateur(Long id);

    List<UtilisateurResponse> obtenirTousLesUtilisateurs();

    UtilisateurResponse modifierUtilisateur(Long id, UpdateUtilisateurRequest request);

    void supprimerUtilisateur(Long id);

    UtilisateurResponse obtenirParEmail(String email);

    Utilisateur trouverParEmail(String email);

}