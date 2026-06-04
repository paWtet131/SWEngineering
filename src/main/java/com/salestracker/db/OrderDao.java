package com.salestracker.db;

import com.salestracker.model.Order;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Работа с таблицей заказов (orders).
 * Сохранение выполняется через MERGE, поэтому повторная синхронизация
 * не создаёт дубликатов уже загруженных заказов (раздел 9.2 спецификации).
 */
public class OrderDao {

    /**
     * Сохранить пачку заказов. Существующие заказы (тот же order_id + shop_id)
     * обновляются, новые — добавляются.
     */
    public void saveAll(List<Order> orders) {
        String sql = """
                MERGE INTO orders
                    (order_id, shop_id, marketplace, article, name, order_date, quantity)
                KEY (order_id, shop_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Order o : orders) {
                ps.setString(1, o.getOrderId());
                ps.setInt(2, o.getShopId());
                ps.setString(3, o.getMarketplace().name());
                ps.setString(4, o.getArticle());
                ps.setString(5, o.getName());
                ps.setDate(6, Date.valueOf(o.getDate()));
                ps.setInt(7, o.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить заказы: " + e.getMessage(), e);
        }
    }

    /** Общее количество заказов в БД (для статусной строки). */
    public long count() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось посчитать заказы: " + e.getMessage(), e);
        }
    }
}
