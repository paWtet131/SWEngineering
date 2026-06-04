package com.salestracker.service;

import com.salestracker.db.OrderDao;
import com.salestracker.db.ShopDao;
import com.salestracker.marketplace.MarketplaceClient;
import com.salestracker.marketplace.MarketplaceException;
import com.salestracker.marketplace.OzonClient;
import com.salestracker.marketplace.WildberriesClient;
import com.salestracker.model.Marketplace;
import com.salestracker.model.Order;
import com.salestracker.model.Shop;

import java.time.LocalDate;
import java.util.List;

/**
 * Синхронизация заказов: выгрузка заказов с маркетплейсов и сохранение их в БД.
 * <p>
 * Сервис обходит все привязанные магазины, для каждого подбирает нужный
 * адаптер ({@link WildberriesClient} или {@link OzonClient}) и сохраняет
 * полученные заказы через {@link OrderDao}.
 */
public class SyncService {

    /** На сколько дней назад загружать заказы при синхронизации. */
    private static final int SYNC_DAYS = 30;

    private final ShopDao shopDao = new ShopDao();
    private final OrderDao orderDao = new OrderDao();

    /**
     * Синхронизирует все привязанные магазины за последние {@value #SYNC_DAYS} дней.
     * <p>
     * Сохранение выполняется оператором {@code MERGE}, поэтому повторная
     * синхронизация не создаёт дубликатов. Если по одному из магазинов
     * произошла ошибка, она пробрасывается наверх, но ранее сохранённые
     * данные других магазинов не теряются (требование НФР-04).
     *
     * @return общее число загруженных заказов
     * @throws MarketplaceException если API маркетплейса недоступен или вернул ошибку
     */
    public int syncAll() throws MarketplaceException {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(SYNC_DAYS);

        int total = 0;
        for (Shop shop : shopDao.findAll()) {
            MarketplaceClient client = clientFor(shop.getMarketplace());
            List<Order> orders = client.fetchOrders(shop, from, to);
            if (!orders.isEmpty()) {
                orderDao.saveAll(orders);   // MERGE: дубликаты не создаются
            }
            total += orders.size();
        }
        return total;
    }

    /**
     * Подбирает клиент-адаптер под маркетплейс.
     *
     * @param marketplace маркетплейс
     * @return адаптер для работы с API этого маркетплейса
     */
    private MarketplaceClient clientFor(Marketplace marketplace) {
        return switch (marketplace) {
            case WB -> new WildberriesClient();
            case OZON -> new OzonClient();
        };
    }
}
