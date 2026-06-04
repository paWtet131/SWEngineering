package com.salestracker.marketplace;

/**
 * Ошибка обращения к API маркетплейса
 * (недоступность, таймаут, неверный ключ, ошибочный ответ).
 */
public class MarketplaceException extends Exception {

    public MarketplaceException(String message) {
        super(message);
    }

    public MarketplaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
