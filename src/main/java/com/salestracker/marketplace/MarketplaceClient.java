package com.salestracker.marketplace;

import com.salestracker.model.Order;
import com.salestracker.model.Shop;

import java.time.LocalDate;
import java.util.List;

/**
 * Общий интерфейс адаптера маркетплейса (НФР-07).
 * Чтобы добавить новый маркетплейс, достаточно создать новый класс,
 * реализующий этот интерфейс, не меняя код отчётов и хранения.
 */
public interface MarketplaceClient {

    /**
     * Выгрузить заказы магазина за период [from; to].
     *
     * @param shop магазин с ключами доступа
     * @param from начало периода (включительно)
     * @param to   конец периода (включительно)
     * @return список заказов
     * @throws MarketplaceException при ошибке или недоступности API
     */
    List<Order> fetchOrders(Shop shop, LocalDate from, LocalDate to) throws MarketplaceException;
}
