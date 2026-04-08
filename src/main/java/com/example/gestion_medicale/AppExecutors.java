package com.example.gestion_medicale;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppExecutors {
    private static final ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(4);

    private AppExecutors() {}

    public static ExecutorService db() {
        return DB_EXECUTOR;
    }

    public static void shutdown() {
        DB_EXECUTOR.shutdown();
    }
}

