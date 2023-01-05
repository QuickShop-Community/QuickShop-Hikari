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
    @Nullable
    private String context;

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
        if (exceptedDuration == null) {
            return false;
        }
        return getTimePassed().compareTo(exceptedDuration) > 0;
    }

    @NotNull
    public Duration getTimePassed() {
        return Duration.between(startTime, Instant.now());
    }

    public void setContext(@Nullable String context) {
        this.context = context;
    }

    @Override
    public void close() {
        Duration passedDuration = getTimePassed();
        String passed = passedDuration.toMillis() + "ms";
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("The task [").append(name).append("] ");
        if (context != null) {
            messageBuilder.append("(").append(context).append(") ");
        }
        messageBuilder.append("has finished in ").append(passed).append(".");
        Level level = Level.INFO;
        if (isReachedLimit()) {
            messageBuilder.append(" OVER LIMIT! The excepted time cost should less than ").append(exceptedDuration.toMillis()).append("ms.");
            level = Level.WARNING;
        }
        Log.performance(level, messageBuilder.toString());
    }
}
