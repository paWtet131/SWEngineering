package com.salestracker.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Тесты кодирования API-ключей (требование НФР-03).
 */
class KeyCodecTest {

    @Test
    @DisplayName("После encode -> decode возвращается исходный ключ")
    void decodeReturnsOriginalAfterEncode() {
        String key = "WB-secret-key-12345";
        assertEquals(key, KeyCodec.decode(KeyCodec.encode(key)));
    }

    @Test
    @DisplayName("Закодированный ключ отличается от исходного текста")
    void encodeChangesText() {
        String key = "my-api-key";
        assertNotEquals(key, KeyCodec.encode(key));
    }

    @Test
    @DisplayName("Кодирование null даёт пустую строку")
    void encodeNullGivesEmptyString() {
        assertEquals("", KeyCodec.encode(null));
    }

    @Test
    @DisplayName("Декодирование пустой строки даёт пустую строку")
    void decodeEmptyGivesEmptyString() {
        assertEquals("", KeyCodec.decode(""));
    }

    @Test
    @DisplayName("Кириллический ключ переживает кодирование без потерь")
    void roundTripKeepsCyrillic() {
        String key = "ключ-Ozon-Клиент-2026";
        assertEquals(key, KeyCodec.decode(KeyCodec.encode(key)));
    }
}
