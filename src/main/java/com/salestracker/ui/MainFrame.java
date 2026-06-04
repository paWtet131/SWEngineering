package com.salestracker.ui;

import com.salestracker.db.OrderDao;
import com.salestracker.service.SyncScheduler;
import com.salestracker.service.SyncService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Главное окно приложения.
 * Содержит панель синхронизации сверху и вкладки с экранами:
 * «Настройки», «Отчёт за день», «Отчёт за период», «Аналитика».
 */
public class MainFrame extends JFrame {

    private final SyncService syncService = new SyncService();
    private final OrderDao orderDao = new OrderDao();
    private final SyncScheduler scheduler;

    private final JLabel statusLabel = new JLabel("Готово");
    private final JLabel countLabel = new JLabel();
    private final JButton syncButton = new JButton("Синхронизировать");

    private final DayReportPanel dayPanel = new DayReportPanel();
    private final PeriodReportPanel periodPanel = new PeriodReportPanel();
    private final AnalyticsPanel analyticsPanel = new AnalyticsPanel();

    public MainFrame() {
        super("SalesTracker — учёт продаж по артикулам");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);

        // Автоматическая синхронизация по таймеру (ФР-06а).
        scheduler = new SyncScheduler(syncService, this::onSchedulerStatus);
        scheduler.start();

        // Корректно останавливаем планировщик при закрытии окна.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                scheduler.stop();
            }
        });

        updateOrderCount();
    }

    /** Верхняя панель: кнопка ручной синхронизации (ФР-06) и статусная строка. */
    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        syncButton.addActionListener(e -> manualSync());
        toolbar.add(syncButton);
        toolbar.add(statusLabel);
        toolbar.add(new JLabel("   "));
        toolbar.add(countLabel);
        return toolbar;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        // При изменении магазинов в настройках обновляем фильтры на всех вкладках.
        SettingsPanel settingsPanel = new SettingsPanel(this::refreshShops);
        tabs.addTab("Настройки", settingsPanel);
        tabs.addTab("Отчёт за день", dayPanel);
        tabs.addTab("Отчёт за период", periodPanel);
        tabs.addTab("Аналитика", analyticsPanel);
        return tabs;
    }

    /** ФР-06: ручной запуск синхронизации. Выполняется в фоне, чтобы не «замораживать» окно. */
    private void manualSync() {
        syncButton.setEnabled(false);
        statusLabel.setText("Идёт синхронизация...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return syncService.syncAll();
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    statusLabel.setText("Синхронизация завершена. Получено заказов: " + count);
                    updateOrderCount();
                } catch (Exception e) {
                    // ФР-16: показываем сообщение об ошибке API.
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    UiUtil.showError(MainFrame.this, cause.getMessage());
                    statusLabel.setText("Синхронизация не выполнена");
                } finally {
                    syncButton.setEnabled(true);
                }
            }
        }.execute();
    }

    /** Сообщение от планировщика приходит из фонового потока — выводим его в потоке UI. */
    private void onSchedulerStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            updateOrderCount();
        });
    }

    private void updateOrderCount() {
        countLabel.setText("Всего заказов в БД: " + orderDao.count());
    }

    /** Обновить фильтры магазинов на всех вкладках отчётов. */
    private void refreshShops() {
        dayPanel.refreshShops();
        periodPanel.refreshShops();
        analyticsPanel.refreshShops();
    }
}
