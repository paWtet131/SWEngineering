package com.salestracker.service;

import com.salestracker.db.Database;
import com.salestracker.model.ReportTable;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Построение отчётов по заказам, накопленным в базе данных.
 * <p>
 * Все отчёты возвращаются в едином виде {@link ReportTable} (заголовки столбцов
 * и строки данных). Параметр {@code shopIds} во всех методах задаёт фильтр
 * по магазинам (требование ФР-09): пустой список или {@code null} означает
 * «все магазины».
 */
public class ReportService {

    /**
     * Строит отчёт за выбранный день в разрезе
     * «артикул — название — маркетплейс — количество» (требование ФР-08).
     *
     * @param day     дата, за которую строится отчёт
     * @param shopIds идентификаторы магазинов для фильтра (пустой список — все)
     * @return отчёт, отсортированный по убыванию количества
     */
    public ReportTable dayReport(LocalDate day, List<Integer> shopIds) {
        ReportTable table = new ReportTable("Артикул", "Название", "Маркетплейс", "Количество");
        String sql = "SELECT article, name, marketplace, SUM(quantity) AS qty "
                + "FROM orders WHERE order_date = ? " + shopFilter(shopIds)
                + " GROUP BY article, name, marketplace ORDER BY qty DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(day));
            applyShopIds(ps, 2, shopIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    table.addRow(rs.getString("article"), rs.getString("name"),
                            rs.getString("marketplace"), rs.getInt("qty"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка построения отчёта за день: " + e.getMessage(), e);
        }
        return table;
    }

    /**
     * Строит сводный отчёт за период с разбивкой по дням и артикулам
     * (требование ФР-10).
     *
     * @param from    начало периода (включительно)
     * @param to      конец периода (включительно)
     * @param shopIds идентификаторы магазинов для фильтра (пустой список — все)
     * @return отчёт, отсортированный по дате и убыванию количества
     */
    public ReportTable periodReport(LocalDate from, LocalDate to, List<Integer> shopIds) {
        ReportTable table = new ReportTable("Дата", "Артикул", "Название", "Маркетплейс", "Количество");
        String sql = "SELECT order_date, article, name, marketplace, SUM(quantity) AS qty "
                + "FROM orders WHERE order_date BETWEEN ? AND ? " + shopFilter(shopIds)
                + " GROUP BY order_date, article, name, marketplace "
                + "ORDER BY order_date, qty DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            applyShopIds(ps, 3, shopIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    table.addRow(rs.getDate("order_date").toLocalDate(), rs.getString("article"),
                            rs.getString("name"), rs.getString("marketplace"), rs.getInt("qty"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка построения отчёта за период: " + e.getMessage(), e);
        }
        return table;
    }

    /**
     * Строит список топ-N артикулов по количеству продаж за период
     * (требование ФР-12).
     *
     * @param from    начало периода (включительно)
     * @param to      конец периода (включительно)
     * @param shopIds идентификаторы магазинов для фильтра (пустой список — все)
     * @param n       размер топа (по требованию — от 5 до 50)
     * @return отчёт из не более чем N строк, отсортированный по убыванию количества
     */
    public ReportTable topReport(LocalDate from, LocalDate to, List<Integer> shopIds, int n) {
        ReportTable table = new ReportTable("Артикул", "Название", "Количество");
        String sql = "SELECT article, name, SUM(quantity) AS qty "
                + "FROM orders WHERE order_date BETWEEN ? AND ? " + shopFilter(shopIds)
                + " GROUP BY article, name ORDER BY qty DESC LIMIT ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            int next = applyShopIds(ps, 3, shopIds);
            ps.setInt(next, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    table.addRow(rs.getString("article"), rs.getString("name"), rs.getInt("qty"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка построения топа артикулов: " + e.getMessage(), e);
        }
        return table;
    }

    /**
     * Строит таблицу сравнения двух периодов по артикулам с колонками
     * «Период 1», «Период 2», «Δ количество», «Δ %» (требование ФР-11).
     *
     * @param p1From  начало первого периода
     * @param p1To    конец первого периода
     * @param p2From  начало второго периода
     * @param p2To    конец второго периода
     * @param shopIds идентификаторы магазинов для фильтра (пустой список — все)
     * @return отчёт сравнения, отсортированный по убыванию изменения количества
     */
    public ReportTable comparisonReport(LocalDate p1From, LocalDate p1To,
                                        LocalDate p2From, LocalDate p2To,
                                        List<Integer> shopIds) {
        return buildComparison(p1From, p1To, p2From, p2To, shopIds, false);
    }

    /**
     * Строит список артикулов с отрицательной динамикой продаж между двумя
     * периодами, отсортированный по величине падения в процентах (требование ФР-13).
     *
     * @param p1From  начало первого периода
     * @param p1To    конец первого периода
     * @param p2From  начало второго периода
     * @param p2To    конец второго периода
     * @param shopIds идентификаторы магазинов для фильтра (пустой список — все)
     * @return отчёт только с просевшими артикулами (сильнее всего упавшие — сверху)
     */
    public ReportTable negativeDynamicsReport(LocalDate p1From, LocalDate p1To,
                                              LocalDate p2From, LocalDate p2To,
                                              List<Integer> shopIds) {
        return buildComparison(p1From, p1To, p2From, p2To, shopIds, true);
    }

    // ----------------------------------------------------------------------------
    // Вспомогательные методы
    // ----------------------------------------------------------------------------

    /**
     * Считает суммарные продажи по артикулам за период.
     *
     * @param from    начало периода
     * @param to      конец периода
     * @param shopIds фильтр по магазинам
     * @return отображение «артикул → (название, количество)»
     */
    private Map<String, Agg> aggregate(LocalDate from, LocalDate to, List<Integer> shopIds) {
        Map<String, Agg> map = new LinkedHashMap<>();
        String sql = "SELECT article, name, SUM(quantity) AS qty "
                + "FROM orders WHERE order_date BETWEEN ? AND ? " + shopFilter(shopIds)
                + " GROUP BY article, name";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            applyShopIds(ps, 3, shopIds);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("article"),
                            new Agg(rs.getString("name"), rs.getInt("qty")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка агрегации продаж: " + e.getMessage(), e);
        }
        return map;
    }

    /**
     * Общая логика сравнения двух периодов (для ФР-11 и ФР-13).
     *
     * @param onlyNegative {@code true} — оставить только артикулы с падением продаж
     * @return таблица сравнения
     */
    private ReportTable buildComparison(LocalDate p1From, LocalDate p1To,
                                        LocalDate p2From, LocalDate p2To,
                                        List<Integer> shopIds, boolean onlyNegative) {
        Map<String, Agg> first = aggregate(p1From, p1To, shopIds);
        Map<String, Agg> second = aggregate(p2From, p2To, shopIds);

        // Объединяем артикулы из обоих периодов.
        Map<String, String> names = new LinkedHashMap<>();
        first.forEach((art, a) -> names.put(art, a.name));
        second.forEach((art, a) -> names.putIfAbsent(art, a.name));

        List<Object[]> rows = new ArrayList<>();
        for (Map.Entry<String, String> e : names.entrySet()) {
            String article = e.getKey();
            int q1 = first.containsKey(article) ? first.get(article).qty : 0;
            int q2 = second.containsKey(article) ? second.get(article).qty : 0;
            int delta = q2 - q1;
            double deltaPercent = percent(q1, q2);

            if (onlyNegative && delta >= 0) {
                continue;   // для отчёта по падению оставляем только отрицательную динамику
            }
            rows.add(new Object[]{article, e.getValue(), q1, q2, delta, round1(deltaPercent)});
        }

        // Сортировка: для падения — по проценту по возрастанию (сильнее всего упавшие сверху),
        // для обычного сравнения — по абсолютному изменению по убыванию.
        if (onlyNegative) {
            rows.sort((a, b) -> Double.compare((double) a[5], (double) b[5]));
        } else {
            rows.sort((a, b) -> Integer.compare((int) b[4], (int) a[4]));
        }

        ReportTable table = new ReportTable(
                "Артикул", "Название", "Период 1", "Период 2", "Δ количество", "Δ %");
        rows.forEach(table::addRow);
        return table;
    }

    /**
     * Вычисляет изменение в процентах от первого периода ко второму.
     * Обрабатывает деление на ноль: если в первом периоде продаж не было,
     * результат равен 0 % или 100 %.
     *
     * @param q1 количество за первый период
     * @param q2 количество за второй период
     * @return изменение в процентах
     */
    private static double percent(int q1, int q2) {
        if (q1 == 0) {
            return q2 == 0 ? 0.0 : 100.0;
        }
        return (q2 - q1) * 100.0 / q1;
    }

    /**
     * Округляет число до одного знака после запятой.
     *
     * @param value исходное значение
     * @return значение, округлённое до десятых
     */
    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * Возвращает фрагмент SQL для фильтра по магазинам.
     *
     * @param shopIds идентификаторы магазинов
     * @return часть условия {@code AND shop_id IN (...)} либо пустая строка
     */
    private static String shopFilter(List<Integer> shopIds) {
        if (shopIds == null || shopIds.isEmpty()) {
            return "";
        }
        String placeholders = String.join(",", shopIds.stream().map(id -> "?").toList());
        return " AND shop_id IN (" + placeholders + ")";
    }

    /**
     * Подставляет идентификаторы магазинов в запрос, начиная с позиции {@code startIndex}.
     *
     * @param ps         подготовленный запрос
     * @param startIndex номер первого свободного параметра
     * @param shopIds    идентификаторы магазинов
     * @return индекс следующего свободного параметра
     * @throws SQLException при ошибке подстановки параметров
     */
    private static int applyShopIds(PreparedStatement ps, int startIndex, List<Integer> shopIds)
            throws SQLException {
        if (shopIds == null || shopIds.isEmpty()) {
            return startIndex;
        }
        int index = startIndex;
        for (Integer id : shopIds) {
            ps.setInt(index++, id);
        }
        return index;
    }

    /**
     * Промежуточный результат агрегации по одному артикулу.
     *
     * @param name название товара
     * @param qty  суммарное количество за период
     */
    private record Agg(String name, int qty) {
    }
}
