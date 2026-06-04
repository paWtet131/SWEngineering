package com.salestracker.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Простое кодирование API-ключей перед сохранением в БД (НФР-03).
 * Ключи не хранятся открытым текстом — применяется кодирование Base64.
 * Это не криптостойкое шифрование, а защита от случайного просмотра файла БД,
 * чего достаточно для локального учебного приложения.
 */
public final class KeyCodec {

    private KeyCodec() {
    }

    /** Закодировать ключ для сохранения в БД. */
    public static String encode(String plain) {
        if (plain == null) {
            return "";
        }
        return Base64.getEncoder().encodeToString(plain.getBytes(StandardCharsets.UTF_8));
    }

    /** Раскодировать ключ, прочитанный из БД. */
    public static String decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return "";
        }
        byte[] bytes = Base64.getDecoder().decode(encoded);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
