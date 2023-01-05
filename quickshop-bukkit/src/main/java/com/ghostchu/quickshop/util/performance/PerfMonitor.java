package com.ghostchu.quickshop.util.performance;

import com.ghostchu.quickshop.util.logger.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;

public class PerfMonitor implements AutoCloseable {
    private final String name;
    private final Instant startTime;
    @Nullable
    private final Duration exceptedDuration;

    public PerfMonitor() {
        Log.Caller caller = Log.Caller.create();
        this.name = caller.getClassName() + "#" + caller.getMethodName();
        this.startTime = Instant.now();
        this.exceptedDuration = null;
    }

    public PerfMonitor(@NotNull Duration exceptedDuration) {
        Log.Caller caller = Log.Caller.create();
        this.name = caller.getClassName() + "#" + caller.getMethodName();
        this.startTime = Instant.now();
        this.exceptedDuration = exceptedDuration;
    }

    public PerfMonitor(@NotNull String name) {
        this.name = name;
        this.startTime = Instant.now();
        this.exceptedDuration = null;
    }

    public PerfMonitor(@NotNull String name, @NotNull Duration exceptedDuration) {
        this.name = name;
        this.startTime = Instant.now();
        this.exceptedDuration = exceptedDuration;
    }

    @Nullable
    public Duration getExceptedDuration() {
        return exceptedDuration;
    }

    @NotNull
    public Instant getStartTime() {
        return startTime;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public boolean isReachedLimit() {
        if (exceptedDuration == null) return false;
        return getTimePassed().compareTo(exceptedDuration) > 0;
    }

    @NotNull
    public Duration getTimePassed() {
        return Duration.between(startTime, Instant.now());
    }

    @Override
    public void close() {
        Duration passedDuration = getTimePassed();
        String passed = passedDuration.toMillis() + "ms";
        if (exceptedDuration != null) {
            if (getTimePassed().compareTo(exceptedDuration) > 0) {
                Log.performance(Level.WARNING, "Task " + name + " finished in " + passed + ", the excepted duration is " + exceptedDuration + ", performance reached the limit");
            } else {
                Log.performance(Level.INFO, "Task " + name + " finished in " + passed + ".");
            }
        } else {
            Log.performance(Level.INFO, "Task " + name + " finished in " + passed + ".");
        }
    }
}
