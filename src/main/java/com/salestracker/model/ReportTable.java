package com.salestracker.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Универсальное представление любого отчёта в виде таблицы:
 * набор заголовков столбцов и строки данных.
 * <p>
 * Один и тот же объект используется и для показа отчёта в {@code JTable},
 * и для экспорта в XLSX или CSV. За счёт этого код построения отчётов
 * не зависит от способа их отображения и сохранения.
 */
public class ReportTable {

    /** Заголовки столбцов отчёта. */
    private final String[] columns;

    /** Строки данных; каждая строка — массив значений по числу столбцов. */
    private final List<Object[]> rows = new ArrayList<>();

    /**
     * Создаёт пустой отчёт с заданными заголовками столбцов.
     *
     * @param columns заголовки столбцов в порядке слева направо
     */
    public ReportTable(String... columns) {
        this.columns = columns;
    }

    /**
     * Добавляет строку отчёта.
     *
     * @param values значения ячеек строки в порядке столбцов
     */
    public void addRow(Object... values) {
        rows.add(values);
    }

    /**
     * @return заголовки столбцов отчёта
     */
    public String[] getColumns() {
        return columns;
    }

    /**
     * @return строки данных отчёта
     */
    public List<Object[]> getRows() {
        return rows;
    }
}
