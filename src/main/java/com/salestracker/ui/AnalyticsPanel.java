package com.salestracker.ui;

import com.salestracker.service.ReportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Экран «Аналитика»: топ-N артикулов (ФР-12), сравнение двух периодов (ФР-11)
 * и список артикулов с отрицательной динамикой (ФР-13).
 * Тип анализа выбирается из выпадающего списка.
 */
public class AnalyticsPanel extends JPanel {

    private static final String TOP = "Топ артикулов (период 1)";
    private static final String COMPARE = "Сравнение периодов 1 и 2";
    private static final String NEGATIVE = "Отрицательная динамика (период 1 → 2)";

    private final ReportService reportService = new ReportService();

    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{TOP, COMPARE, NEGATIVE});

    private final JTextField p1From = new JTextField(LocalDate.now().minusDays(14).toString(), 9);
    private final JTextField p1To = new JTextField(LocalDate.now().minusDays(7).toString(), 9);
    private final JTextField p2From = new JTextField(LocalDate.now().minusDays(7).toString(), 9);
    private final JTextField p2To = new JTextField(LocalDate.now().toString(), 9);
    private final JSpinner topN = new JSpinner(new SpinnerNumberModel(10, 5, 50, 1));

    private final ShopFilter shopFilter = new ShopFilter();
    private final ReportView reportView = new ReportView();

    public AnalyticsPanel() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buildControls(), BorderLayout.NORTH);
        add(reportView, BorderLayout.CENTER);
    }

    private JPanel buildControls() {
        JPanel top = new JPanel(new BorderLayout(5, 5));

        JPanel typeLine = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typeLine.add(new JLabel("Тип анализа:"));
        typeLine.add(typeBox);
        typeLine.add(new JLabel("N (для топа):"));
        typeLine.add(topN);
        JButton build = new JButton("Построить");
        build.addActionListener(e -> buildReport());
        typeLine.add(build);

        JPanel periodsLine = new JPanel(new FlowLayout(FlowLayout.LEFT));
        periodsLine.add(new JLabel("Период 1:"));
        periodsLine.add(p1From);
        periodsLine.add(new JLabel("—"));
        periodsLine.add(p1To);
        periodsLine.add(new JLabel("   Период 2:"));
        periodsLine.add(p2From);
        periodsLine.add(new JLabel("—"));
        periodsLine.add(p2To);

        top.add(typeLine, BorderLayout.NORTH);
        top.add(periodsLine, BorderLayout.CENTER);
        top.add(shopFilter, BorderLayout.SOUTH);
        return top;
    }

    private void buildReport() {
        String type = (String) typeBox.getSelectedItem();
        List<Integer> shops = shopFilter.getSelectedShopIds();
        try {
            if (TOP.equals(type)) {
                LocalDate from = LocalDate.parse(p1From.getText().trim());
                LocalDate to = LocalDate.parse(p1To.getText().trim());
                checkOrder(from, to);
                int n = (Integer) topN.getValue();
                reportView.show(reportService.topReport(from, to, shops, n));
            } else {
                LocalDate f1 = LocalDate.parse(p1From.getText().trim());
                LocalDate t1 = LocalDate.parse(p1To.getText().trim());
                LocalDate f2 = LocalDate.parse(p2From.getText().trim());
                LocalDate t2 = LocalDate.parse(p2To.getText().trim());
                checkOrder(f1, t1);
                checkOrder(f2, t2);
                checkEqualLength(f1, t1, f2, t2);

                if (COMPARE.equals(type)) {
                    reportView.show(reportService.comparisonReport(f1, t1, f2, t2, shops));
                } else {
                    reportView.show(reportService.negativeDynamicsReport(f1, t1, f2, t2, shops));
                }
            }
        } catch (java.time.format.DateTimeParseException e) {
            UiUtil.showError(this, "Неверный формат даты. Используйте ГГГГ-ММ-ДД.");
        } catch (IllegalArgumentException e) {
            UiUtil.showError(this, e.getMessage());
        } catch (Exception e) {
            UiUtil.showError(this, e.getMessage());
        }
    }

    private void checkOrder(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("Дата «по» не может быть раньше даты «с».");
        }
    }

    /** ФР-11: периоды для сравнения должны быть равной длительности. */
    private void checkEqualLength(LocalDate f1, LocalDate t1, LocalDate f2, LocalDate t2) {
        long len1 = ChronoUnit.DAYS.between(f1, t1);
        long len2 = ChronoUnit.DAYS.between(f2, t2);
        if (len1 != len2) {
            throw new IllegalArgumentException("Периоды 1 и 2 должны быть одинаковой длительности.");
        }
    }

    /** Обновить список магазинов в фильтре. */
    public void refreshShops() {
        shopFilter.reload();
    }
}
