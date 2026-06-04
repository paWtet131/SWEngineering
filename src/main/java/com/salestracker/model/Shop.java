package com.salestracker.model;

/**
 * Привязанный магазин маркетплейса.
 * <p>
 * Для Wildberries используется только {@code apiKey}.
 * Для Ozon дополнительно нужен {@code clientId} (идентификатор клиента).
 * API-ключ хранится в базе в закодированном виде (требование НФР-03).
 */
public class Shop {

    /** Первичный ключ в БД (заполняется автоматически при сохранении). */
    private int id;

    /** Маркетплейс магазина. */
    private Marketplace marketplace;

    /** Произвольное название магазина, заданное пользователем. */
    private String name;

    /** API-ключ доступа к маркетплейсу. */
    private String apiKey;

    /** Client-Id (только для Ozon; для Wildberries не используется). */
    private String clientId;

    /** Создаёт пустой магазин (поля заполняются через сеттеры). */
    public Shop() {
    }

    /**
     * Создаёт магазин со всеми атрибутами.
     *
     * @param id          идентификатор магазина в БД
     * @param marketplace маркетплейс
     * @param name        название магазина
     * @param apiKey      API-ключ доступа
     * @param clientId    Client-Id для Ozon (для Wildberries — {@code null})
     */
    public Shop(int id, Marketplace marketplace, String name, String apiKey, String clientId) {
        this.id = id;
        this.marketplace = marketplace;
        this.name = name;
        this.apiKey = apiKey;
        this.clientId = clientId;
    }

    /** @return идентификатор магазина в БД */
    public int getId() {
        return id;
    }

    /** @param id идентификатор магазина в БД */
    public void setId(int id) {
        this.id = id;
    }

    /** @return маркетплейс магазина */
    public Marketplace getMarketplace() {
        return marketplace;
    }

    /** @param marketplace маркетплейс магазина */
    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    /** @return название магазина */
    public String getName() {
        return name;
    }

    /** @param name название магазина */
    public void setName(String name) {
        this.name = name;
    }

    /** @return API-ключ доступа к маркетплейсу */
    public String getApiKey() {
        return apiKey;
    }

    /** @param apiKey API-ключ доступа к маркетплейсу */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /** @return Client-Id для Ozon (для Wildberries — {@code null}) */
    public String getClientId() {
        return clientId;
    }

    /** @param clientId Client-Id для Ozon */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Возвращает подпись магазина для выпадающих списков,
     * например «Мой магазин (Wildberries)».
     *
     * @return строковое представление магазина
     */
    @Override
    public String toString() {
        return name + " (" + marketplace.getTitle() + ")";
    }
}
