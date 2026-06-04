package com.salestracker.ui;

import com.salestracker.service.ReportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;

/**
 * Экран «Отчёт за день»: показывает продажи по артикулам за выбранную дату
 * с фильтром по магазинам (ФР-08, ФР-09).
 */
public class DayReportPanel extends JPanel {

    private final ReportService reportService = new ReportService();
    private final JTextField dateField = new JTextField(LocalDate.now().toString(), 10);
    private final ShopFilter shopFilter = new ShopFilter();
    private final ReportView reportView = new ReportView();

    public DayReportPanel() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buildControls(), BorderLayout.NORTH);
        add(reportView, BorderLayout.CENTER);
    }

    private JPanel buildControls() {
        JPanel top = new JPanel(new BorderLayout(10, 0));

        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT));
        line.add(new JLabel("Дата (ГГГГ-ММ-ДД):"));
        line.add(dateField);
        JButton build = new JButton("Построить отчёт");
        build.addActionListener(e -> buildReport());
        line.add(build);

        top.add(line, BorderLayout.NORTH);
        top.add(shopFilter, BorderLayout.CENTER);
        return top;
    }

    private void buildReport() {
        LocalDate day;
        try {
            day = LocalDate.parse(dateField.getText().trim());
        } catch (Exception e) {
            UiUtil.showError(this, "Неверный формат даты. Используйте ГГГГ-ММ-ДД, например 2026-05-30.");
            return;
        }
        try {
            reportView.show(reportService.dayReport(day, shopFilter.getSelectedShopIds()));
        } catch (Exception e) {
            UiUtil.showError(this, e.getMessage());
        }
    }

    /** Обновить список магазинов в фильтре. */
    public void refreshShops() {
        shopFilter.reload();
    }
}
