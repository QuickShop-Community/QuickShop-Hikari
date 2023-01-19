package com.ghostchu.quickshop.util.performance;

import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class BulkExecutor extends BukkitRunnable {
    private final int bulkSize;
    private final Queue<Runnable> tasks = new LinkedList<>();
    private final Consumer<BulkExecutor> callback;
    private Instant startTime;

    public BulkExecutor(int amount, @NotNull Consumer<BulkExecutor> finishCallback) {
        this.bulkSize = amount;
        this.callback = finishCallback;
    }

    public void addTask(@NotNull Runnable runnable) {
        if (this.isCancelled()) throw new IllegalStateException("This executor has been cancelled.");
        this.tasks.add(runnable);
    }

    @NotNull
    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public void run() {
        if (startTime == null) startTime = Instant.now();
        int bulkCounter = 0;
        while (bulkCounter < bulkSize) {
            Runnable task = tasks.poll();
            if (task == null) {
                break;
            }
            task.run();
            bulkCounter++;
        }
        if (tasks.isEmpty()) {
            this.cancel();
            this.callback.accept(this);
        }
    }
}
