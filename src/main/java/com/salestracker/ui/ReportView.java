package com.salestracker.ui;

import com.salestracker.model.ReportTable;
import com.salestracker.service.ExportService;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;

/**
 * Область просмотра отчёта: таблица с сортировкой по столбцам
 * и кнопки экспорта в XLSX и CSV (ФР-14, ФР-15).
 * Используется всеми экранами отчётов.
 */
public class ReportView extends JPanel {

    private final JTable table = new JTable();
    private final ExportService exportService = new ExportService();
    private ReportTable current;

    public ReportView() {
        super(new BorderLayout());

        table.setAutoCreateRowSorter(true);   // сортировка по клику на заголовок столбца
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton xlsxButton = new JButton("Экспорт в XLSX");
        xlsxButton.addActionListener(e -> export(true));

        JButton csvButton = new JButton("Экспорт в CSV");
        csvButton.addActionListener(e -> export(false));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(xlsxButton);
        buttons.add(csvButton);
        add(buttons, BorderLayout.SOUTH);
    }

    /** Показать отчёт в таблице. */
    public void show(ReportTable report) {
        this.current = report;
        table.setModel(UiUtil.toTableModel(report));
    }

    private void export(boolean xlsx) {
        if (current == null || current.getRows().isEmpty()) {
            UiUtil.showError(this, "Нет данных для экспорта. Сначала постройте отчёт.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        String ext = xlsx ? "xlsx" : "csv";
        chooser.setSelectedFile(new File("report." + ext));
        chooser.setFileFilter(new FileNameExtensionFilter(ext.toUpperCase() + " файлы", ext));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith("." + ext)) {
            file = new File(file.getParentFile(), file.getName() + "." + ext);
        }

        try {
            if (xlsx) {
                exportService.toXlsx(current, file);
            } else {
                exportService.toCsv(current, file);
            }
            UiUtil.showInfo(this, "Отчёт сохранён: " + file.getAbsolutePath());
        } catch (Exception e) {
            UiUtil.showError(this, "Не удалось сохранить файл: " + e.getMessage());
        }
    }
}
