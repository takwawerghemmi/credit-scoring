package com.creditscoring.controller;
import com.creditscoring.dto.reponse.*;
import com.creditscoring.service.*;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.enums.StatutDemande;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DashboardController {
    private final FraudDetectionService fraudDetectionService;
    private final PredictionService predictionService;
    private final CsvExportService csvExportService;
    private final DashboardService dashboardService;
    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;
    private final RecommendationService recommendationService;
    @GetMapping
    public Map<String, Object> getDashboard() {
        return dashboardService.getDashboardData();
    }

    @GetMapping("/statistiques")
    public Map<String, Object> getStatistiques() {
        return dashboardService.getDashboardData();
    }

    @GetMapping("/demandes-par-mois")
    public List<Object[]> getDemandesParMois() {
        return dashboardService.getDemandesParMois();
    }

    @GetMapping("/demandes-par-type")
    public List<Object[]> getDemandesParType() {
        return dashboardService.getDemandesParType();
    }

    @GetMapping("/fraudes")
    public List<Object[]> getFraudes() {
        return dashboardService.getStatistiquesFraude();
    }

    @GetMapping("/clients-par-profession")
    public List<Object[]> getClientsParProfession() {
        return dashboardService.getClientsParProfession();
    }

    @GetMapping("/clients-par-situation")
    public List<Object[]> getClientsParSituation() {
        return dashboardService.getClientsParSituationFamiliale();
    }

    @GetMapping("/kpi")
    public Map<String, Object> getKpi() {
        return dashboardService.getDashboardData();
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        return pdfExportService.exportDashboardPdf();
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel() {
        return excelExportService.exportDashboardExcel();
    }
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        return csvExportService.exportDashboardCsv();
    }

    @GetMapping("/montants-par-mois")
    public List<Object[]> getMontantsParMois() {
        return dashboardService.getEvolutionMontantsAccordes();
    }
    @GetMapping("/score-credit")
    public List<Object[]> getRepartitionScoreCredit() {
        return dashboardService.getRepartitionScoreCredit();
    }
    @GetMapping("/performance-conseillers")
    public List<Object[]> getPerformanceConseillers() {
        return dashboardService.getPerformanceConseillers();
    }
    @GetMapping("/top-clients")
    public List<Object[]> getTopClients() {
        return dashboardService.getTopClients();
    }
    @GetMapping("/top-agences")
    public List<Object[]> getTopAgences() {
        return dashboardService.getTopAgences();
    }
    @GetMapping("/repartition-agences")
    public List<Object[]> getRepartitionParAgence() {
        return dashboardService.getRepartitionParAgence();
    }
    @GetMapping("/top-conseillers")
    public List<Object[]> getTopConseillers() {
        return dashboardService.getTopConseillers();
    }
    @GetMapping("/dernieres-demandes")
    public List<Object[]> getDernieresDemandes() {
        return dashboardService.getDernieresDemandes();
    }
    @GetMapping("/dernieres-alertes-fraude")
    public List<Object[]> getDernieresAlertesFraude() {
        return dashboardService.getDernieresAlertesFraude();
    }
    @GetMapping("/filtre")
    public List<DemandeCredit> filtrerDemandes(

            @RequestParam(required = false)
            StatutDemande statut,

            @RequestParam(required = false)
            String typeCredit) {

        return dashboardService.filtrerDemandes(
                statut,
                typeCredit
        );
    }
    @PostMapping("/prediction")
    public PredictionResponse prediction(@RequestBody DemandeCredit demande) {
        return predictionService.predire(demande);
    }
    @PostMapping("/recommendation")
    public RecommendationResponse recommendation(
            @RequestBody DemandeCredit demande) {

        return recommendationService.genererRecommandations(demande);
    }
    @PostMapping("/fraude")
    public FraudDetectionResponse detecterFraude(
            @RequestBody DemandeCredit demande) {

        return fraudDetectionService.detecterFraude(demande);
    }
    @GetMapping("/statistiques-credits-par-mois")
    public List<Object[]> getStatistiquesCreditsParMois() {
        return dashboardService.getStatistiquesCreditsParMois();
    }
    @GetMapping("/comparaison/mois")
    public List<Object[]> getComparaisonParMois() {
        return dashboardService.getComparaisonParMois();
    }
    @GetMapping("/comparaison/mois/detail")
    public List<ComparaisonMoisResponse> getComparaisonMoisDetail() {

        return dashboardService.getComparaisonMoisDetaillee();

    }
    @GetMapping("/comparaison/annee")
    public List<ComparaisonAnneeResponse> comparaisonAnnee() {
        return dashboardService.getComparaisonParAnnee();
    }
    @GetMapping("/classement-conseillers")
    public List<ClassementConseillerResponse> getClassementConseillers() {

        return dashboardService.getClassementConseillers();

    }
    @GetMapping("/classement-clients")
    public List<ClassementClientResponse> getClassementClients() {

        return dashboardService.getClassementClients();

    }
    @GetMapping("/classement-banques")
    public List<ClassementBanqueResponse> classementBanques(){

        return dashboardService.getClassementBanques();

    }
    @GetMapping("/classement-agences")
    public List<ClassementAgenceResponse> classementAgences(){

        return dashboardService.getClassementAgences();

    }




    @GetMapping("/kpi-avances")
    public KpiAvanceResponse getKpiAvances(){

        return dashboardService.getKpiAvances();

    }
    @GetMapping("/executif")
    public DashboardExecutifResponse getDashboardExecutif() {

        return dashboardService.getDashboardExecutif();

    }
}


