package com.creditscoring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
@Service
@RequiredArgsConstructor
public class CsvExportService {

    private final DashboardService dashboardService;


    public ResponseEntity<byte[]> exportDashboardCsv() {

        byte[] csv = generateDashboardCsv();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=dashboard_credit_scoring.csv"
                )
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);

    }




    public byte[] generateDashboardCsv() {

        try {

            Map<String, Object> dashboard = dashboardService.getDashboardData();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            OutputStreamWriter writer =
                    new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

            writer.write("Indicateur;Valeur\n");

            writer.write("Nombre de clients;" + dashboard.get("nombreClients") + "\n");
            writer.write("Nombre de demandes;" + dashboard.get("nombreDemandes") + "\n");
            writer.write("Nombre de banques;" + dashboard.get("nombreBanques") + "\n");
            writer.write("Nombre d'agences;" + dashboard.get("nombreAgences") + "\n");
            writer.write("Crédits approuvés;" + dashboard.get("nombreCreditsApprouves") + "\n");
            writer.write("Crédits refusés;" + dashboard.get("nombreCreditsRefuses") + "\n");
            writer.write("Demandes en analyse;" + dashboard.get("nombreDemandesEnAnalyse") + "\n");
            writer.write("Montant total accordé;" + dashboard.get("montantTotalAccorde") + "\n");
            writer.write("Montant moyen accordé;" + dashboard.get("montantMoyenAccorde") + "\n");
            writer.write("Taux d'acceptation;" + dashboard.get("tauxAcceptation") + "\n");
            writer.write("Taux de refus;" + dashboard.get("tauxRefus") + "\n");
            writer.write("Alertes fraude;" + dashboard.get("nombreAlertesFraude") + "\n");

            writer.flush();
            writer.close();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erreur génération CSV",
                    e
            );

        }

    }










}