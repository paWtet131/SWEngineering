package com.salestracker;

import com.salestracker.db.Database;
import com.salestracker.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Точка входа в приложение SalesTracker.
 * Инициализирует базу данных и открывает главное окно.
 */
public class Main {

    public static void main(String[] args) {
        // Создаём таблицы в БД при первом запуске (раздел 11.1 спецификации).
        Database.init();

        // Системное оформление окна (более привычный вид под текущую ОС).
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Если оформление недоступно — используем стандартное.
        }

        // Запуск интерфейса в потоке обработки событий Swing.
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
