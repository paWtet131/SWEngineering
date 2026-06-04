package com.salestracker.model;

/**
 * Перечень поддерживаемых маркетплейсов.
 * <p>
 * Чтобы добавить новый маркетплейс, нужно добавить сюда новое значение
 * и создать класс-адаптер, реализующий интерфейс
 * {@code com.salestracker.marketplace.MarketplaceClient}.
 */
public enum Marketplace {

    /** Wildberries. */
    WB("Wildberries"),

    /** Ozon. */
    OZON("Ozon");

    /** Человекочитаемое название площадки. */
    private final String title;

    /**
     * @param title человекочитаемое название маркетплейса
     */
    Marketplace(String title) {
        this.title = title;
    }

    /**
     * Возвращает человекочитаемое название маркетплейса для интерфейса.
     *
     * @return название площадки, например «Wildberries»
     */
    public String getTitle() {
        return title;
    }
}
