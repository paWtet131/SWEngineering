package com.salestracker.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Управление встроенной базой данных H2 (файловый режим).
 * База создаётся автоматически при первом запуске в папке ./data.
 * Доступ к БД выполняется через стандартный JDBC.
 */
public final class Database {

    // Файловая БД H2. AUTO_SERVER позволяет открывать БД из нескольких соединений.
    private static final String URL = "jdbc:h2:./data/salestracker;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private Database() {
    }

    /** Получить новое соединение с БД. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Создать таблицы, если их ещё нет. Вызывается один раз при старте приложения.
     */
    public static void init() {
        String createShops = """
                CREATE TABLE IF NOT EXISTS shops (
                    id          INT AUTO_INCREMENT PRIMARY KEY,
                    marketplace VARCHAR(20)   NOT NULL,
                    name        VARCHAR(255)  NOT NULL,
                    api_key     VARCHAR(4000) NOT NULL,
                    client_id   VARCHAR(255)
                )
                """;

        // Первичный ключ (order_id, shop_id) защищает от дублей при повторной синхронизации.
        String createOrders = """
                CREATE TABLE IF NOT EXISTS orders (
                    order_id    VARCHAR(255) NOT NULL,
                    shop_id     INT          NOT NULL,
                    marketplace VARCHAR(20)  NOT NULL,
                    article     VARCHAR(255),
                    name        VARCHAR(500),
                    order_date  DATE         NOT NULL,
                    quantity    INT          NOT NULL DEFAULT 1,
                    PRIMARY KEY (order_id, shop_id)
                )
                """;

        // Индекс по дате ускоряет построение отчётов (НФР-01).
        String createIndex =
                "CREATE INDEX IF NOT EXISTS idx_orders_date ON orders(order_date)";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.execute(createShops);
            st.execute(createOrders);
            st.execute(createIndex);
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось инициализировать базу данных: " + e.getMessage(), e);
        }
    }
}
