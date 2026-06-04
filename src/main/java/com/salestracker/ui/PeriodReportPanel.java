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
import java.time.temporal.ChronoUnit;

/**
 * Экран «Отчёт за период»: сводный отчёт по дням и артикулам
 * за период от 1 до 90 дней с фильтром по магазинам (ФР-10).
 */
public class PeriodReportPanel extends JPanel {

    private final ReportService reportService = new ReportService();
    private final JTextField fromField = new JTextField(LocalDate.now().minusDays(7).toString(), 10);
    private final JTextField toField = new JTextField(LocalDate.now().toString(), 10);
    private final ShopFilter shopFilter = new ShopFilter();
    private final ReportView reportView = new ReportView();

    public PeriodReportPanel() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buildControls(), BorderLayout.NORTH);
        add(reportView, BorderLayout.CENTER);
    }

    private JPanel buildControls() {
        JPanel top = new JPanel(new BorderLayout(10, 0));

        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT));
        line.add(new JLabel("С (ГГГГ-ММ-ДД):"));
        line.add(fromField);
        line.add(new JLabel("По:"));
        line.add(toField);
        JButton build = new JButton("Построить отчёт");
        build.addActionListener(e -> buildReport());
        line.add(build);

        top.add(line, BorderLayout.NORTH);
        top.add(shopFilter, BorderLayout.CENTER);
        return top;
    }

    private void buildReport() {
        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(fromField.getText().trim());
            to = LocalDate.parse(toField.getText().trim());
        } catch (Exception e) {
            UiUtil.showError(this, "Неверный формат даты. Используйте ГГГГ-ММ-ДД.");
            return;
        }
        if (to.isBefore(from)) {
            UiUtil.showError(this, "Дата «По» не может быть раньше даты «С».");
            return;
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days < 1 || days > 90) {
            UiUtil.showError(this, "Период должен быть от 1 до 90 дней.");
            return;
        }
        try {
            reportView.show(reportService.periodReport(from, to, shopFilter.getSelectedShopIds()));
        } catch (Exception e) {
            UiUtil.showError(this, e.getMessage());
        }
    }

    /** Обновить список магазинов в фильтре. */
    public void refreshShops() {
        shopFilter.reload();
    }
}
