package com.salestracker.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты универсального представления отчёта.
 */
class ReportTableTest {

    @Test
    @DisplayName("Заголовки столбцов сохраняются в заданном порядке")
    void storesColumns() {
        ReportTable table = new ReportTable("Артикул", "Название", "Количество");
        assertArrayEquals(new String[]{"Артикул", "Название", "Количество"}, table.getColumns());
    }

    @Test
    @DisplayName("Новый отчёт не содержит строк")
    void isEmptyByDefault() {
        ReportTable table = new ReportTable("Артикул");
        assertTrue(table.getRows().isEmpty());
    }

    @Test
    @DisplayName("Строки добавляются и доступны по порядку")
    void storesRowsInOrder() {
        ReportTable table = new ReportTable("Артикул", "Количество");
        table.addRow("ART-1", 8);
        table.addRow("ART-2", 3);

        assertEquals(2, table.getRows().size());
        assertEquals("ART-1", table.getRows().get(0)[0]);
        assertEquals(3, table.getRows().get(1)[1]);
    }
}
