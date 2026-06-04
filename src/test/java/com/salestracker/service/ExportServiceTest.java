package com.salestracker.service;

import com.salestracker.model.ReportTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты экспорта отчётов в XLSX и CSV (требования ФР-14, ФР-15).
 */
class ExportServiceTest {

    private final ExportService exportService = new ExportService();

    /** Небольшой отчёт-образец для проверок. */
    private ReportTable sampleReport() {
        ReportTable table = new ReportTable("Артикул", "Количество");
        table.addRow("ART-1", 8);
        table.addRow("ART-2", 3);
        return table;
    }

    @Test
    @DisplayName("CSV содержит заголовок и данные отчёта")
    void csvContainsHeaderAndData(@TempDir Path dir) throws Exception {
        File file = dir.resolve("report.csv").toFile();
        exportService.toCsv(sampleReport(), file);

        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains("Артикул"));
        assertTrue(content.contains("ART-1"));
        assertTrue(content.contains("8"));
    }

    @Test
    @DisplayName("В CSV одна строка заголовка и по строке на запись")
    void csvHasLinePerRow(@TempDir Path dir) throws Exception {
        File file = dir.resolve("rows.csv").toFile();
        exportService.toCsv(sampleReport(), file);

        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        assertEquals(3, lines.size()); // заголовок + две строки данных
    }

    @Test
    @DisplayName("XLSX-файл создаётся и не пустой")
    void xlsxFileIsCreatedAndNotEmpty(@TempDir Path dir) throws Exception {
        File file = dir.resolve("report.xlsx").toFile();
        exportService.toXlsx(sampleReport(), file);

        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
}
