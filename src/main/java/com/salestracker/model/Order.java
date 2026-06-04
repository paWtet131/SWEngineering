package com.salestracker.model;

import java.time.LocalDate;

/**
 * Один заказ, выгруженный с маркетплейса.
 * <p>
 * Поле {@code orderId} — уникальный идентификатор заказа на стороне маркетплейса.
 * По нему выполняется проверка на дубликаты при повторной синхронизации,
 * поэтому один и тот же заказ не попадает в базу дважды.
 */
public class Order {

    /** Уникальный идентификатор заказа на стороне маркетплейса. */
    private String orderId;

    /** Идентификатор магазина, которому принадлежит заказ. */
    private int shopId;

    /** Маркетплейс, с которого выгружен заказ. */
    private Marketplace marketplace;

    /** Артикул продавца. */
    private String article;

    /** Название товара. */
    private String name;

    /** Дата заказа. */
    private LocalDate date;

    /** Количество единиц товара в заказе. */
    private int quantity;

    /**
     * Создаёт заказ со всеми атрибутами.
     *
     * @param orderId     уникальный идентификатор заказа на маркетплейсе
     * @param shopId      идентификатор магазина-владельца
     * @param marketplace маркетплейс заказа
     * @param article     артикул продавца
     * @param name        название товара
     * @param date        дата заказа
     * @param quantity    количество единиц товара
     */
    public Order(String orderId, int shopId, Marketplace marketplace,
                 String article, String name, LocalDate date, int quantity) {
        this.orderId = orderId;
        this.shopId = shopId;
        this.marketplace = marketplace;
        this.article = article;
        this.name = name;
        this.date = date;
        this.quantity = quantity;
    }

    /** @return уникальный идентификатор заказа на маркетплейсе */
    public String getOrderId() {
        return orderId;
    }

    /** @return идентификатор магазина-владельца */
    public int getShopId() {
        return shopId;
    }

    /** @return маркетплейс заказа */
    public Marketplace getMarketplace() {
        return marketplace;
    }

    /** @return артикул продавца */
    public String getArticle() {
        return article;
    }

    /** @return название товара */
    public String getName() {
        return name;
    }

    /** @return дата заказа */
    public LocalDate getDate() {
        return date;
    }

    /** @return количество единиц товара в заказе */
    public int getQuantity() {
        return quantity;
    }
}
