package com.creditscoring.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final DashboardService dashboardService;
    public byte[] generateDashboardPdf() {

        try {

            Map<String, Object> data = dashboardService.getDashboardData();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4);

            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 15, Font.BOLD);
            Font cellFont = new Font(Font.HELVETICA, 12);

            Paragraph title = new Paragraph(
                    "RAPPORT DASHBOARD BI - CREDIT SCORING",
                    titleFont
            );

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Date : " + LocalDate.now()));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Indicateurs clés", sectionFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);

            table.setWidthPercentage(100);

            table.setWidths(new float[]{4,2});

            addRow(table,"Nombre clients",data.get("nombreClients"));
            addRow(table,"Nombre demandes",data.get("nombreDemandes"));
            addRow(table,"Nombre banques",data.get("nombreBanques"));
            addRow(table,"Nombre agences",data.get("nombreAgences"));
            addRow(table,"Crédits approuvés",data.get("nombreCreditsApprouves"));
            addRow(table,"Crédits refusés",data.get("nombreCreditsRefuses"));
            addRow(table,"Demandes en analyse",data.get("nombreDemandesEnAnalyse"));
            addRow(table,"Montant total accordé",data.get("montantTotalAccorde"));
            addRow(table,"Taux d'acceptation",data.get("tauxAcceptation")+" %");
            addRow(table,"Taux de refus",data.get("tauxRefus")+" %");
            addRow(table,"Montant moyen",data.get("montantMoyenAccorde"));
            addRow(table,"Alertes fraude",data.get("nombreAlertesFraude"));

            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Rapport généré automatiquement par le système Credit Scoring.",
                    cellFont));

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erreur lors de la génération du PDF",
                    e
            );

        }

    }
    public ResponseEntity<byte[]> exportDashboardPdf() {

        byte[] pdf = generateDashboardPdf();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.set(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=dashboard_credit_scoring.pdf"
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);

    }

    private void addRow(PdfPTable table,String key,Object value){

        PdfPCell cell1 = new PdfPCell(new Phrase(key));

        PdfPCell cell2 = new PdfPCell(
                new Phrase(value == null ? "-" : value.toString())
        );

        table.addCell(cell1);
        table.addCell(cell2);

    }

}