package com.creditscoring.scheduler;

import com.creditscoring.service.CsvExportService;
import com.creditscoring.service.ExcelExportService;
import com.creditscoring.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardScheduler {

    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;
    private final CsvExportService csvExportService;

    @Scheduled(cron = "0 0 3 * * *")
    public void genererRapports() {

        try {

            Path reportsFolder = Paths.get("reports");

            if (!Files.exists(reportsFolder)) {
                Files.createDirectories(reportsFolder);
            }

            String date = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            Files.write(
                    reportsFolder.resolve("dashboard_" + date + ".pdf"),
                    pdfExportService.generateDashboardPdf()
            );

            Files.write(
                    reportsFolder.resolve("dashboard_" + date + ".xlsx"),
                    excelExportService.generateDashboardExcel()
            );

            Files.write(
                    reportsFolder.resolve("dashboard_" + date + ".csv"),
                    csvExportService.generateDashboardCsv()
            );

            log.info("==========================================");
            log.info("Rapports générés automatiquement.");
            log.info("Date : {}", LocalDateTime.now());
            log.info("==========================================");

        } catch (IOException e) {

            log.error("Erreur génération automatique des rapports", e);

        }

    }

}