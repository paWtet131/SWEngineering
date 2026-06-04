package com.salestracker.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Планировщик автоматической синхронизации (требование ФР-06а).
 * <p>
 * Пока приложение запущено, планировщик раз в час без участия пользователя
 * запускает синхронизацию заказов в отдельном фоновом потоке-демоне.
 * Результат каждой синхронизации (или текст ошибки) передаётся слушателю.
 */
public class SyncScheduler {

    /** Интервал автосинхронизации по умолчанию, в минутах. */
    private static final long INTERVAL_MINUTES = 60;

    private final SyncService syncService;
    private final Consumer<String> statusListener;   // куда сообщать результат/ошибку
    private ScheduledExecutorService executor;

    /**
     * Создаёт планировщик.
     *
     * @param syncService    сервис синхронизации, который будет запускаться по таймеру
     * @param statusListener получатель сообщений о результате синхронизации или об ошибке
     */
    public SyncScheduler(SyncService syncService, Consumer<String> statusListener) {
        this.syncService = syncService;
        this.statusListener = statusListener;
    }

    /**
     * Запускает периодическую синхронизацию в фоновом потоке-демоне.
     * Поток-демон не мешает завершению приложения при закрытии окна.
     */
    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "auto-sync");
            thread.setDaemon(true);   // не мешает завершению приложения
            return thread;
        });
        executor.scheduleAtFixedRate(this::runSync,
                INTERVAL_MINUTES, INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    /** Останавливает планировщик при закрытии приложения. */
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    /** Выполняет одну синхронизацию и сообщает результат слушателю. */
    private void runSync() {
        try {
            int count = syncService.syncAll();
            statusListener.accept("Автосинхронизация выполнена, заказов получено: " + count);
        } catch (Exception e) {
            // При сбое API данные в БД сохраняются (НФР-04), сообщаем об ошибке.
            statusListener.accept("Автосинхронизация: ошибка — " + e.getMessage());
        }
    }
}
