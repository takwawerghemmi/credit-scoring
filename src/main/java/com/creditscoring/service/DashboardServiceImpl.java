package com.creditscoring.service;
import com.creditscoring.dto.reponse.ClassementBanqueResponse;
import com.creditscoring.dto.reponse.ComparaisonAnneeResponse;
import com.creditscoring.dto.reponse.ClassementConseillerResponse;
import com.creditscoring.dto.reponse.ClassementClientResponse;
import com.creditscoring.dto.reponse.ClassementAgenceResponse;
import com.creditscoring.dto.reponse.KpiAvanceResponse;
import com.creditscoring.dto.reponse.ComparaisonMoisResponse;
import com.creditscoring.dto.reponse.DashboardExecutifResponse;
import org.springframework.cache.annotation.Cacheable;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.enums.StatutDemande;
import com.creditscoring.repository.AgenceRepository;
import com.creditscoring.repository.AlerteFraudeRepository;
import com.creditscoring.repository.BanqueRepository;
import com.creditscoring.repository.ClientRepository;
import com.creditscoring.repository.DemandeCreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClientRepository clientRepository;
    private final DemandeCreditRepository demandeCreditRepository;
    private final BanqueRepository banqueRepository;
    private final AgenceRepository agenceRepository;
    private final AlerteFraudeRepository alerteFraudeRepository;

    @Override
    @Cacheable(value = "dashboard")

    public Map<String, Object> getDashboardData() {

        Map<String, Object> dashboard = new HashMap<>();

        // Statistiques générales
        dashboard.put("nombreClients", clientRepository.count());
        dashboard.put("nombreDemandes", demandeCreditRepository.nombreDemandes());
        dashboard.put("nombreBanques", banqueRepository.count());
        dashboard.put("nombreAgences", agenceRepository.count());

        // KPI
        Long totalDemandes = demandeCreditRepository.nombreDemandes();
        Long approuvees = demandeCreditRepository.nombreCreditsApprouves();
        Long refusees = demandeCreditRepository.nombreCreditsRefuses();

        dashboard.put("nombreCreditsApprouves", approuvees);
        dashboard.put("nombreCreditsRefuses", refusees);
        dashboard.put("nombreDemandesEnAnalyse",
                demandeCreditRepository.nombreCreditsEnAnalyse());

        Double montantTotal = demandeCreditRepository.montantTotalAccorde();
        dashboard.put("montantTotalAccorde", montantTotal);

        // Taux
        double tauxAcceptation = 0.0;
        double tauxRefus = 0.0;

        if (totalDemandes != null && totalDemandes > 0) {
            tauxAcceptation = (approuvees * 100.0) / totalDemandes;
            tauxRefus = (refusees * 100.0) / totalDemandes;
        }

        dashboard.put("tauxAcceptation", tauxAcceptation);
        dashboard.put("tauxRefus", tauxRefus);

        // Montant moyen
        double montantMoyen = 0.0;

        if (montantTotal != null && approuvees != null && approuvees > 0) {
            montantMoyen = montantTotal / approuvees;
        }

        dashboard.put("montantMoyenAccorde", montantMoyen);

        // Nombre d'alertes fraude
        dashboard.put("nombreAlertesFraude",
                alerteFraudeRepository.nombreAlertesFraude());
        dashboard.put("scoreCreditMoyen",
                clientRepository.scoreCreditMoyen());

        dashboard.put("revenuMoyen",
                clientRepository.revenuMoyen());

        dashboard.put("montantMaximumDemande",
                demandeCreditRepository.montantMaximumDemande());

        dashboard.put("montantMinimumDemande",
                demandeCreditRepository.montantMinimumDemande());
        dashboard.put(
                "montantMaximumAccorde",
                demandeCreditRepository.montantMaximumAccorde()
        );

        dashboard.put(
                "montantMinimumAccorde",
                demandeCreditRepository.montantMinimumAccorde()
        );

        dashboard.put(
                "montantMoyenDashboard",
                demandeCreditRepository.montantMoyenAccordeDashboard()
        );

        dashboard.put(
                "demandesAujourdHui",
                demandeCreditRepository.demandesAujourdHui()
        );
        return dashboard;
    }

    @Override
    public List<Object[]> getDemandesParMois() {
        return demandeCreditRepository.demandesParMois();
    }

    @Override
    public List<Object[]> getDemandesParType() {
        return demandeCreditRepository.demandesParTypeCredit();
    }

    @Override
    public List<Object[]> getStatistiquesFraude() {
        return alerteFraudeRepository.statistiquesFraude();
    }

    @Override
    public List<Object[]> getClientsParProfession() {
        return clientRepository.clientsParProfession();
    }

    @Override
    public List<Object[]> getClientsParSituationFamiliale() {
        return clientRepository.clientsParSituationFamiliale();
    }
    @Override
    public List<Object[]> getRepartitionScoreCredit() {
        return clientRepository.repartitionScoreCredit();
    }
    @Override
    public List<Object[]> getEvolutionMontantsAccordes() {
        return demandeCreditRepository.evolutionMontantsAccordes();

    }
    @Override
    public List<Object[]> getPerformanceConseillers() {
        return demandeCreditRepository.performanceConseillers();
    }
    @Override
    public List<Object[]> getTopClients() {
        return clientRepository.topClients();
    }
    @Override
    public List<Object[]> getTopAgences() {
        return agenceRepository.topAgences();
    }
    @Override
    public List<Object[]> getRepartitionParAgence() {
        return agenceRepository.repartitionParAgence();
    }
    @Override
    public List<Object[]> getTopConseillers() {
        return demandeCreditRepository.topConseillers();
    }
    @Override
    public List<Object[]> getDernieresDemandes() {
        return demandeCreditRepository.dernieresDemandes();
    }
    @Override
    public List<Object[]> getDernieresAlertesFraude() {
        return alerteFraudeRepository.dernieresAlertesFraude();
    }
    @Override
    public List<DemandeCredit> filtrerDemandes(
            StatutDemande statut,
            String typeCredit) {

        return demandeCreditRepository.filtrerDemandes(
                statut,
                typeCredit
        );
    }
    @Override
    public List<Object[]> getStatistiquesCreditsParMois() {
        return demandeCreditRepository.statistiquesCreditsParMois();
    }
    @Override
    public List<Object[]> getComparaisonParMois() {
        return demandeCreditRepository.comparaisonParMois();
    }
    @Override
    public List<ComparaisonMoisResponse> getComparaisonMoisDetaillee() {

        List<Object[]> data = demandeCreditRepository.comparaisonParMois();

        List<ComparaisonMoisResponse> resultat = new ArrayList<>();

        Double precedent = null;

        for (Object[] ligne : data) {

            Integer mois = (Integer) ligne[0];

            Long nombre = ((Number) ligne[1]).longValue();

            Double montant = ((Number) ligne[2]).doubleValue();

            Double moyenne = ((Number) ligne[3]).doubleValue();

            Double evolution = 0.0;

            if (precedent != null && precedent != 0) {

                evolution = ((montant - precedent) / precedent) * 100;

            }

            precedent = montant;

            resultat.add(
                    new ComparaisonMoisResponse(
                            mois,
                            nombre,
                            montant,
                            moyenne,
                            Math.round(evolution * 100.0) / 100.0
                    )
            );

        }

        return resultat;

    }
    @Override
    public List<ComparaisonAnneeResponse> getComparaisonParAnnee() {

        List<Object[]> data = demandeCreditRepository.comparaisonParAnnee();

        List<ComparaisonAnneeResponse> resultat = new ArrayList<>();

        for (Object[] row : data) {

            Integer annee = ((Number) row[0]).intValue();

            Long demandes = ((Number) row[1]).longValue();

            Long approuvees = ((Number) row[2]).longValue();

            Long refusees = ((Number) row[3]).longValue();

            Double montant = ((Number) row[4]).doubleValue();

            Double taux = demandes == 0 ? 0 : (approuvees * 100.0) / demandes;

            resultat.add(new ComparaisonAnneeResponse(
                    annee,
                    demandes,
                    approuvees,
                    refusees,
                    montant,
                    Math.round(taux * 100.0) / 100.0
            ));
        }

        return resultat;
    }
    @Override
    public List<ClassementConseillerResponse> getClassementConseillers() {

        List<Object[]> data = demandeCreditRepository.classementConseillers();

        List<ClassementConseillerResponse> resultat = new ArrayList<>();

        int rang = 1;

        for (Object[] row : data) {

            String conseiller = (String) row[0];

            Long demandes = ((Number) row[1]).longValue();

            Long approuvees = ((Number) row[2]).longValue();

            Long refusees = ((Number) row[3]).longValue();

            Double montant = ((Number) row[4]).doubleValue();

            Double taux = demandes == 0 ? 0 : (approuvees * 100.0) / demandes;

            resultat.add(

                    new ClassementConseillerResponse(

                            rang++,

                            conseiller,

                            demandes,

                            approuvees,

                            refusees,

                            Math.round(taux * 100.0) / 100.0,

                            montant

                    )

            );

        }

        return resultat;

    }
    @Override
    public List<ClassementClientResponse> getClassementClients() {

        List<Object[]> data = clientRepository.classementClients();

        List<ClassementClientResponse> resultat = new ArrayList<>();

        int rang = 1;

        for (Object[] row : data) {

            resultat.add(

                    new ClassementClientResponse(

                            rang++,

                            (String) row[0],

                            ((Number) row[1]).longValue(),

                            ((Number) row[2]).doubleValue(),

                            ((Number) row[3]).doubleValue()

                    )

            );

        }

        return resultat;

    }
    @Override
    public List<ClassementBanqueResponse> getClassementBanques() {

        List<Object[]> data = banqueRepository.classementBanques();

        List<ClassementBanqueResponse> resultat = new ArrayList<>();

        int rang = 1;

        for(Object[] row : data){

            resultat.add(

                    new ClassementBanqueResponse(

                            rang++,

                            (String) row[0],

                            ((Number)row[1]).longValue(),

                            ((Number)row[2]).longValue(),

                            ((Number)row[3]).longValue(),

                            ((Number)row[4]).doubleValue()

                    )

            );

        }

        return resultat;


    }@Override
    public List<ClassementAgenceResponse> getClassementAgences(){

        List<Object[]> data = agenceRepository.classementAgences();

        List<ClassementAgenceResponse> resultat = new ArrayList<>();

        int rang = 1;

        for(Object[] row : data){

            resultat.add(

                    new ClassementAgenceResponse(

                            rang++,

                            (String)row[0],

                            ((Number)row[1]).longValue(),

                            ((Number)row[2]).longValue(),

                            ((Number)row[3]).longValue(),

                            ((Number)row[4]).doubleValue()

                    )

            );

        }

        return resultat;

    }
    @Override
    public KpiAvanceResponse getKpiAvances() {

        Long aujourdHui = demandeCreditRepository.demandesAujourdHui();

        Long semaine = demandeCreditRepository.demandesCetteSemaine();

        Long mois = demandeCreditRepository.demandesCeMois();

        Long total = demandeCreditRepository.nombreDemandes();

        Long acceptees = demandeCreditRepository.creditsAcceptes();

        Double montantMoyen =
                demandeCreditRepository.montantMoyenAccordeDashboard();

        Long fraudes =
                alerteFraudeRepository.nombreFraudes();

        Double tauxAcceptation = 0.0;

        if (total != null && total > 0) {
            tauxAcceptation = (acceptees * 100.0) / total;
        }

        Double tauxFraude = 0.0;

        if (total != null && total > 0) {
            tauxFraude = (fraudes * 100.0) / total;
        }

        Double croissance = 0.0;

        List<Object[]> evolution =
                demandeCreditRepository.evolutionMensuelleMontants();

        if (evolution != null && evolution.size() >= 2) {

            Double precedent =
                    ((Number) evolution.get(evolution.size() - 2)[1]).doubleValue();

            Double actuel =
                    ((Number) evolution.get(evolution.size() - 1)[1]).doubleValue();

            if (precedent > 0) {
                croissance = ((actuel - precedent) / precedent) * 100;
            }
        }

        croissance = Math.round(croissance * 100.0) / 100.0;

        return new KpiAvanceResponse(
                aujourdHui,
                semaine,
                mois,
                croissance,
                montantMoyen,
                Math.round(tauxAcceptation * 100.0) / 100.0,
                Math.round(tauxFraude * 100.0) / 100.0
        );
    }
    @Override
    public DashboardExecutifResponse getDashboardExecutif() {

        DashboardExecutifResponse dashboard = new DashboardExecutifResponse();

        dashboard.setKpi(getKpiAvances());

        dashboard.setComparaisonMois(getComparaisonMoisDetaillee());

        dashboard.setComparaisonAnnee(getComparaisonParAnnee());

        dashboard.setClassementClients(getClassementClients());

        dashboard.setClassementConseillers(getClassementConseillers());

        dashboard.setClassementBanques(getClassementBanques());

        dashboard.setClassementAgences(getClassementAgences());

        dashboard.setTopClients(getTopClients());

        dashboard.setTopAgences(getTopAgences());

        dashboard.setTopConseillers(getTopConseillers());

        dashboard.setFraudes(getStatistiquesFraude());

        return dashboard;

    }

}