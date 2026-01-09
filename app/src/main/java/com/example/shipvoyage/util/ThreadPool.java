package com.example.shipvoyage.util;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ThreadPool {
    private static final ExecutorService executor = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));
    public static ExecutorService getExecutor() {
        return executor;
    }
    public static void shutdown() {
        executor.shutdown();
    }
}