package com.salestracker.db;

import com.salestracker.model.Marketplace;
import com.salestracker.model.Shop;
import com.salestracker.util.KeyCodec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Работа с таблицей магазинов (shops).
 * API-ключи кодируются перед сохранением и раскодируются при чтении.
 */
public class ShopDao {

    /** Сохранить новый магазин и вернуть его с присвоенным id. */
    public Shop insert(Shop shop) {
        String sql = "INSERT INTO shops(marketplace, name, api_key, client_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, shop.getMarketplace().name());
            ps.setString(2, shop.getName());
            ps.setString(3, KeyCodec.encode(shop.getApiKey()));
            ps.setString(4, shop.getClientId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    shop.setId(keys.getInt(1));
                }
            }
            return shop;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить магазин: " + e.getMessage(), e);
        }
    }

    /** Удалить магазин и все его заказы. */
    public void delete(int shopId) {
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM orders WHERE shop_id = ?")) {
                ps.setInt(1, shopId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM shops WHERE id = ?")) {
                ps.setInt(1, shopId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось удалить магазин: " + e.getMessage(), e);
        }
    }

    /** Получить список всех привязанных магазинов. */
    public List<Shop> findAll() {
        List<Shop> result = new ArrayList<>();
        String sql = "SELECT id, marketplace, name, api_key, client_id FROM shops ORDER BY id";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Shop shop = new Shop(
                        rs.getInt("id"),
                        Marketplace.valueOf(rs.getString("marketplace")),
                        rs.getString("name"),
                        KeyCodec.decode(rs.getString("api_key")),
                        rs.getString("client_id")
                );
                result.add(shop);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось загрузить список магазинов: " + e.getMessage(), e);
        }
        return result;
    }
}
