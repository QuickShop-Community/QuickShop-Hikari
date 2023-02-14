package com.ghostchu.quickshop.util.performance;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class BatchBukkitExecutor<T> {
    private final Queue<T> tasks = new LinkedList<>();
    private int maxTickMsUsage = 15;
    private boolean started = false;
    private Instant startTime = Instant.MIN;

    public BatchBukkitExecutor() {
    }

    public BatchBukkitExecutor(int maxTickMsUsage) {
        this.maxTickMsUsage = maxTickMsUsage;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void addTask(@NotNull T task) {
        if (started) throw new IllegalStateException("This batch task has been started");
        this.tasks.add(task);
    }

    @SafeVarargs
    public final void addTasks(@NotNull T... tasks) {
        if (started) throw new IllegalStateException("This batch task has been started");
        this.tasks.addAll(Arrays.asList(tasks));
    }

    public void addTasks(@NotNull Collection<T> tasks) {
        if (started) throw new IllegalStateException("This batch task has been started");
        this.tasks.addAll(tasks);
    }

    public void startHandle(Plugin plugin, Consumer<T> consumer) {
        startHandle(plugin, consumer, () -> {
        });
    }


    public void startHandle(Plugin plugin, Consumer<T> consumer, Runnable callback) {
        if (started) throw new IllegalStateException("This batch task has been handled");
        started = true;
        startTime = Instant.now();
        new BatchBukkitTask<>(tasks, consumer, maxTickMsUsage, callback).runTaskTimer(plugin, 1L, 1L);
    }

    static class BatchBukkitTask<T> extends BukkitRunnable {
        public final Queue<T> tasks;
        public final Consumer<T> consumer;
        public final int maxTickMsUsage;
        private final Runnable callback;

        public BatchBukkitTask(Queue<T> tasks, Consumer<T> consumer, int maxTickMsUsage, Runnable callback) {
            this.tasks = tasks;
            this.consumer = consumer;
            this.maxTickMsUsage = maxTickMsUsage;
            this.callback = callback;
        }

        @Override
        public void run() {
            while (!tasks.isEmpty()) {
                long startAt = System.currentTimeMillis();
                do {
                    consumer.accept(tasks.poll());
                } while (System.currentTimeMillis() - startAt < maxTickMsUsage && !tasks.isEmpty());
            }
            this.cancel();
            callback.run();
        }
    }

}
