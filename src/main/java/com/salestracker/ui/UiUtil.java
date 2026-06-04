package com.salestracker.ui;

import com.salestracker.model.ReportTable;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;

/**
 * Небольшие вспомогательные методы для интерфейса.
 */
public final class UiUtil {

    private UiUtil() {
    }

    /** Преобразовать отчёт в модель таблицы для JTable (ячейки только для чтения). */
    public static DefaultTableModel toTableModel(ReportTable report) {
        DefaultTableModel model = new DefaultTableModel(report.getColumns(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] row : report.getRows()) {
            model.addRow(row);
        }
        return model;
    }

    /** Показать сообщение об ошибке (ФР-16). */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    /** Показать информационное сообщение. */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Сообщение", JOptionPane.INFORMATION_MESSAGE);
    }
}
