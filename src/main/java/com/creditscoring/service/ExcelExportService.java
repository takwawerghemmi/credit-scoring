package com.creditscoring.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final DashboardService dashboardService;

    public ResponseEntity<byte[]> exportDashboardExcel() {

        byte[] excel = generateDashboardExcel();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=dashboard_credit_scoring.xlsx"
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);

    }

    private int addRow(Sheet sheet, int rowIndex, String label, Object value) {

        Row row = sheet.createRow(rowIndex);

        row.createCell(0).setCellValue(label);

        row.createCell(1).setCellValue(
                value == null ? "" : value.toString()
        );

        return rowIndex + 1;
    }
    public byte[] generateDashboardExcel() {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Dashboard BI");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row titleRow = sheet.createRow(0);

            Cell titleCell = titleRow.createCell(0);

            titleCell.setCellValue("Dashboard BI - Credit Scoring");

            titleCell.setCellStyle(headerStyle);

            Row headerRow = sheet.createRow(2);

            headerRow.createCell(0).setCellValue("Indicateur");
            headerRow.createCell(1).setCellValue("Valeur");

            Map<String, Object> dashboard = dashboardService.getDashboardData();

            int rowIndex = 3;

            rowIndex = addRow(sheet,rowIndex,"Nombre de clients",dashboard.get("nombreClients"));
            rowIndex = addRow(sheet,rowIndex,"Nombre de demandes",dashboard.get("nombreDemandes"));
            rowIndex = addRow(sheet,rowIndex,"Nombre de banques",dashboard.get("nombreBanques"));
            rowIndex = addRow(sheet,rowIndex,"Nombre d'agences",dashboard.get("nombreAgences"));
            rowIndex = addRow(sheet,rowIndex,"Crédits approuvés",dashboard.get("nombreCreditsApprouves"));
            rowIndex = addRow(sheet,rowIndex,"Crédits refusés",dashboard.get("nombreCreditsRefuses"));
            rowIndex = addRow(sheet,rowIndex,"Demandes en analyse",dashboard.get("nombreDemandesEnAnalyse"));
            rowIndex = addRow(sheet,rowIndex,"Montant total accordé",dashboard.get("montantTotalAccorde"));
            rowIndex = addRow(sheet,rowIndex,"Taux d'acceptation",dashboard.get("tauxAcceptation"));
            rowIndex = addRow(sheet,rowIndex,"Taux de refus",dashboard.get("tauxRefus"));
            rowIndex = addRow(sheet,rowIndex,"Montant moyen",dashboard.get("montantMoyenAccorde"));
            rowIndex = addRow(sheet,rowIndex,"Alertes fraude",dashboard.get("nombreAlertesFraude"));

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Erreur génération Excel",
                    e
            );

        }

    }
}