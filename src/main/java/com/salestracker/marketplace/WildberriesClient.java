package com.salestracker.marketplace;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.salestracker.model.Marketplace;
import com.salestracker.model.Order;
import com.salestracker.model.Shop;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер Wildberries Statistics API.
 * Метод выгрузки заказов: GET /api/v1/supplier/orders?dateFrom=ГГГГ-ММ-ДД
 * Авторизация — через заголовок Authorization с API-ключом продавца.
 */
public class WildberriesClient implements MarketplaceClient {

    private static final String URL =
            "https://statistics-api.wildberries.ru/api/v1/supplier/orders";

    // Таймаут запроса — 15 секунд (ФР-16): дольше ждать ответ не нужно.
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    @Override
    public List<Order> fetchOrders(Shop shop, LocalDate from, LocalDate to) throws MarketplaceException {
        // WB принимает только дату начала; конечную дату фильтруем у себя.
        String url = URL + "?dateFrom=" + from + "&flag=0";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", shop.getApiKey())
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new MarketplaceException(
                    "Wildberries API недоступен: " + e.getMessage(), e);
        }

        if (response.statusCode() != 200) {
            throw new MarketplaceException(
                    "Wildberries API вернул ошибку (код " + response.statusCode() + "). "
                            + "Проверьте API-ключ.");
        }

        return parse(response.body(), shop, from, to);
    }

    /** Разобрать JSON-массив заказов и отфильтровать по конечной дате периода. */
    private List<Order> parse(String body, Shop shop, LocalDate from, LocalDate to)
            throws MarketplaceException {
        List<Order> orders = new ArrayList<>();
        try {
            JsonArray array = JsonParser.parseString(body).getAsJsonArray();
            for (JsonElement el : array) {
                JsonObject o = el.getAsJsonObject();

                // Отменённые заказы не учитываем в продажах.
                if (o.has("isCancel") && o.get("isCancel").getAsBoolean()) {
                    continue;
                }

                LocalDate date = parseDate(o.get("date").getAsString());
                if (date.isBefore(from) || date.isAfter(to)) {
                    continue;
                }

                String orderId = o.get("srid").getAsString();        // уникальный id заказа
                String article = getString(o, "supplierArticle");    // артикул продавца
                String name = getString(o, "subject");               // предмет (название)

                orders.add(new Order(orderId, shop.getId(), Marketplace.WB,
                        article, name, date, 1));
            }
        } catch (Exception e) {
            throw new MarketplaceException(
                    "Не удалось разобрать ответ Wildberries: " + e.getMessage(), e);
        }
        return orders;
    }

    private static String getString(JsonObject o, String field) {
        return o.has(field) && !o.get(field).isJsonNull() ? o.get(field).getAsString() : "";
    }

    /** WB возвращает дату в формате "2021-11-23T11:24:32". */
    private static LocalDate parseDate(String raw) {
        return LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME).toLocalDate();
    }
}
