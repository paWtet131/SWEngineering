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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер Ozon Seller API.
 * Метод выгрузки отправлений: POST /v3/posting/fbs/list.
 * Авторизация — через заголовки Client-Id и Api-Key.
 */
public class OzonClient implements MarketplaceClient {

    private static final String URL = "https://api-seller.ozon.ru/v3/posting/fbs/list";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final int PAGE_SIZE = 1000;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    @Override
    public List<Order> fetchOrders(Shop shop, LocalDate from, LocalDate to) throws MarketplaceException {
        List<Order> orders = new ArrayList<>();
        int offset = 0;

        // Постранично забираем отправления, пока приходят полные страницы.
        while (true) {
            String json = requestPage(shop, from, to, offset);
            int received = parsePage(json, shop, orders);
            if (received < PAGE_SIZE) {
                break;          // последняя страница
            }
            offset += PAGE_SIZE;
            if (offset > 100_000) {
                break;          // страховка от бесконечного цикла
            }
        }
        return orders;
    }

    /** Отправить запрос на одну страницу отправлений. */
    private String requestPage(Shop shop, LocalDate from, LocalDate to, int offset)
            throws MarketplaceException {
        String body = """
                {
                  "dir": "ASC",
                  "filter": { "since": "%sT00:00:00.000Z", "to": "%sT23:59:59.999Z" },
                  "limit": %d,
                  "offset": %d,
                  "with": {}
                }
                """.formatted(from, to, PAGE_SIZE, offset);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Client-Id", shop.getClientId() == null ? "" : shop.getClientId())
                .header("Api-Key", shop.getApiKey())
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new MarketplaceException("Ozon API недоступен: " + e.getMessage(), e);
        }

        if (response.statusCode() != 200) {
            throw new MarketplaceException(
                    "Ozon API вернул ошибку (код " + response.statusCode() + "). "
                            + "Проверьте Client-Id и Api-Key.");
        }
        return response.body();
    }

    /**
     * Разобрать страницу отправлений. Возвращает количество отправлений на странице
     * (нужно, чтобы понять, есть ли ещё страницы).
     */
    private int parsePage(String body, Shop shop, List<Order> target) throws MarketplaceException {
        try {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            JsonObject result = root.getAsJsonObject("result");
            if (result == null) {
                return 0;
            }
            JsonArray postings = result.getAsJsonArray("postings");
            if (postings == null) {
                return 0;
            }

            for (JsonElement el : postings) {
                JsonObject posting = el.getAsJsonObject();
                String orderId = posting.get("posting_number").getAsString();  // уникальный id
                LocalDate date = OffsetDateTime.parse(
                        posting.get("in_process_at").getAsString()).toLocalDate();

                JsonArray products = posting.getAsJsonArray("products");
                for (JsonElement pe : products) {
                    JsonObject p = pe.getAsJsonObject();
                    String article = getString(p, "offer_id");   // артикул продавца
                    String name = getString(p, "name");
                    int qty = p.has("quantity") ? p.get("quantity").getAsInt() : 1;

                    // Если в отправлении несколько товаров — у каждого свой id строки.
                    String lineId = orderId + "_" + article;
                    target.add(new Order(lineId, shop.getId(), Marketplace.OZON,
                            article, name, date, qty));
                }
            }
            return postings.size();
        } catch (Exception e) {
            throw new MarketplaceException(
                    "Не удалось разобрать ответ Ozon: " + e.getMessage(), e);
        }
    }

    private static String getString(JsonObject o, String field) {
        return o.has(field) && !o.get(field).isJsonNull() ? o.get(field).getAsString() : "";
    }
}
