package com.salestracker.service;

import com.salestracker.model.ReportTable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Экспорт отчётов в файлы.
 * <p>
 * Формат XLSX создаётся библиотекой Apache POI (требование ФР-14),
 * формат CSV — штатными средствами Java (требование ФР-15).
 */
public class ExportService {

    /**
     * Сохраняет отчёт в файл формата XLSX (требование ФР-14).
     *
     * @param table отчёт для сохранения
     * @param file  файл назначения
     * @throws IOException при ошибке записи файла
     */
    public void toXlsx(ReportTable table, File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             OutputStream out = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("Отчёт");

            // Строка заголовков.
            Row header = sheet.createRow(0);
            String[] columns = table.getColumns();
            for (int c = 0; c < columns.length; c++) {
                header.createCell(c).setCellValue(columns[c]);
            }

            // Строки данных.
            int r = 1;
            for (Object[] values : table.getRows()) {
                Row row = sheet.createRow(r++);
                for (int c = 0; c < values.length; c++) {
                    writeCell(row.createCell(c), values[c]);
                }
            }

            for (int c = 0; c < columns.length; c++) {
                sheet.autoSizeColumn(c);
            }
            workbook.write(out);
        }
    }

    /**
     * Сохраняет отчёт в файл формата CSV (требование ФР-15).
     * Используется разделитель «;» и кодировка UTF-8 с BOM,
     * чтобы Excel корректно открывал кириллицу.
     *
     * @param table отчёт для сохранения
     * @param file  файл назначения
     * @throws IOException при ошибке записи файла
     */
    public void toCsv(ReportTable table, File file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            // BOM, чтобы Excel корректно открывал кириллицу в UTF-8.
            writer.write('﻿');

            writer.write(joinCsv(table.getColumns()));
            writer.newLine();

            for (Object[] values : table.getRows()) {
                String[] text = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    text[i] = values[i] == null ? "" : values[i].toString();
                }
                writer.write(joinCsv(text));
                writer.newLine();
            }
        }
    }

    /**
     * Записывает значение в ячейку XLSX: числа — как числа, остальное — как текст.
     *
     * @param cell  ячейка
     * @param value значение
     */
    private void writeCell(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * Собирает строку CSV из значений, экранируя разделители и кавычки.
     *
     * @param values значения ячеек строки
     * @return готовая строка CSV
     */
    private String joinCsv(String[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(escape(values[i]));
        }
        return sb.toString();
    }

    /**
     * Экранирует значение CSV, если оно содержит разделитель, кавычку или перенос строки.
     *
     * @param value исходное значение
     * @return экранированное значение
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
