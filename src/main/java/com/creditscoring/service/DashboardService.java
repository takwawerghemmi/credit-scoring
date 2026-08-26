package com.creditscoring.service;
import com.creditscoring.dto.reponse.ClassementBanqueResponse;
import com.creditscoring.dto.reponse.ComparaisonAnneeResponse;
import com.creditscoring.dto.reponse.ClassementConseillerResponse;
import com.creditscoring.dto.reponse.ClassementClientResponse;
import com.creditscoring.dto.reponse.ClassementAgenceResponse;
import com.creditscoring.dto.reponse.KpiAvanceResponse;
import com.creditscoring.dto.reponse.ComparaisonMoisResponse;
import com.creditscoring.dto.reponse.DashboardExecutifResponse;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.enums.StatutDemande;
import java.util.List;
import java.util.Map;


public interface DashboardService {

    Map<String, Object> getDashboardData();

    List<Object[]> getDemandesParMois();

    List<Object[]> getDemandesParType();

    List<Object[]> getStatistiquesFraude();

    List<Object[]> getClientsParProfession();

    List<Object[]> getClientsParSituationFamiliale();
    List<Object[]> getRepartitionScoreCredit();
    List<Object[]> getEvolutionMontantsAccordes();
    List<Object[]> getPerformanceConseillers();
    List<Object[]> getTopClients();
    List<Object[]> getTopAgences();
    List<Object[]> getRepartitionParAgence();
    List<Object[]> getTopConseillers();

    List<Object[]> getDernieresDemandes();
    List<Object[]> getDernieresAlertesFraude();

    List<DemandeCredit> filtrerDemandes(
            StatutDemande statut,
            String typeCredit
    );
    List<Object[]> getStatistiquesCreditsParMois();
    List<Object[]> getComparaisonParMois();
    List<ComparaisonMoisResponse> getComparaisonMoisDetaillee();

    List<ComparaisonAnneeResponse> getComparaisonParAnnee();
    List<ClassementConseillerResponse> getClassementConseillers();
    List<ClassementClientResponse> getClassementClients();

    List<ClassementBanqueResponse> getClassementBanques();
    List<ClassementAgenceResponse> getClassementAgences();

    KpiAvanceResponse getKpiAvances();

    DashboardExecutifResponse getDashboardExecutif();









}
