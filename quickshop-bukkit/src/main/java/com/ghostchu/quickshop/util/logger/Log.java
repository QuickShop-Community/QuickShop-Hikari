package com.ghostchu.quickshop.util.logger;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.common.util.Timer;
import com.ghostchu.quickshop.util.Util;
import com.google.common.collect.EvictingQueue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

public class Log {
    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();
    private static final int BUFFER_SIZE = 500 * Type.values().length;
    private static final Queue<Record> LOGGER_BUFFER = EvictingQueue.create(BUFFER_SIZE);
    private static final boolean DISABLE_LOCATION_RECORDING;
    private static final StackWalker STACK_WALKER = StackWalker.getInstance();

    static {
        // Cannot replace with Util since it depend on this class
        DISABLE_LOCATION_RECORDING = Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.util.logger."));
    }

    public static void cron(@NotNull String message) {
        cron(Level.INFO, message, Caller.create());
    }

    @ApiStatus.Internal
    public static void cron(@NotNull Level level, @NotNull String message, @Nullable Caller caller) {
        LOCK.writeLock().lock();
        try {
            Record recordEntry;
            if (DISABLE_LOCATION_RECORDING) {
                recordEntry = new Record(level, Type.CRON, message, null);
            } else {
                recordEntry = new Record(level, Type.CRON, message, caller);
            }
            LOGGER_BUFFER.offer(recordEntry);
            debugStdOutputs(recordEntry);
        } finally {
            LOCK.writeLock().unlock();
        }

    }

    private static void debugStdOutputs(Record recordEntry) {
        recordEntry.generate().thenAccept(log -> {
            if (Util.isDevMode()) {
                QuickShop.getInstance().logger().info("[DEBUG] " + log);
            }
        });
    }

    public static void cron(@NotNull Level level, @NotNull String message) {
        cron(level, message, Caller.create());
    }

    public static void debug(@NotNull String message) {
        debug(Level.INFO, message, Caller.create());
    }

    @ApiStatus.Internal
    public static void debug(@NotNull Level level, @NotNull String message, @Nullable Caller caller) {
        LOCK.writeLock().lock();
        try {
            Record recordEntry;
            if (DISABLE_LOCATION_RECORDING) {
                recordEntry = new Record(level, Type.DEBUG, message, null);
            } else {
                recordEntry = new Record(level, Type.DEBUG, message, caller);
            }
            LOGGER_BUFFER.offer(recordEntry);
            debugStdOutputs(recordEntry);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    public static void debug(@NotNull Level level, @NotNull String message) {
        debug(level, message, Caller.create());
    }


    public static void privacy(@NotNull String message) {
        privacy(Level.INFO, message, Caller.create());
    }

    @ApiStatus.Internal
    public static void privacy(@NotNull Level level, @NotNull String message, @Nullable Caller caller) {
        LOCK.writeLock().lock();
        try {
            Record recordEntry;
            if (DISABLE_LOCATION_RECORDING) {
                recordEntry = new Record(level, Type.PRIVACY, message, null);
            } else {
                recordEntry = new Record(level, Type.PRIVACY, message, caller);
            }
            LOGGER_BUFFER.offer(recordEntry);
            debugStdOutputs(recordEntry);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    public static void privacy(@NotNull Level level, @NotNull String message) {
        privacy(level, message, Caller.create());
    }


    public static void performance(@NotNull Level level, @NotNull String message, @NotNull Caller caller) {
        LOCK.writeLock().lock();
        try {
            Record recordEntry = new Record(level, Type.PERFORMANCE, message, caller);
            LOGGER_BUFFER.offer(recordEntry);
            debugStdOutputs(recordEntry);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    @NotNull
    public static List<Record> fetchLogs() {
        LOCK.readLock().lock();
        try {
            return new ArrayList<>(LOGGER_BUFFER);
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @NotNull
    public static List<Record> fetchLogs(@NotNull Type type) {
        LOCK.readLock().lock();
        try {
            return LOGGER_BUFFER.stream().filter(recordEntry -> recordEntry.getType() == type).toList();
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @NotNull
    public static List<Record> fetchLogsExclude(@NotNull Type... excludes) {
        LOCK.readLock().lock();
        try {
            List<Record> records = new ArrayList<>();
            for (Record recordEntry : LOGGER_BUFFER) {
                if (ArrayUtils.contains(excludes, recordEntry.getType())) {
                    continue;
                }
                records.add(recordEntry);
            }
            return records;
        } finally {
            LOCK.readLock().unlock();
        }
    }

    @NotNull
    public static List<Record> fetchLogsLevel(@NotNull Type type, @NotNull Level level) {
        LOCK.readLock().lock();
        try {
            return LOGGER_BUFFER.stream().filter(recordEntry -> recordEntry.getType() == type && recordEntry.getLevel() == level).toList();
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public static void permission(@NotNull String message) {
        permission(Level.INFO, message, Caller.create(3, false));
    }

    @ApiStatus.Internal
    public static void permission(@NotNull Level level, @NotNull String message, @Nullable Caller caller) {
        LOCK.writeLock().lock();
        try {
            Record recordEntry;
            if (DISABLE_LOCATION_RECORDING) {
                recordEntry = new Record(level, Type.PERMISSION, message, null);
            } else {
                recordEntry = new Record(level, Type.PERMISSION, message, caller);
            }
            LOGGER_BUFFER.offer(recordEntry);
            debugStdOutputs(recordEntry);
        } finally {
            LOCK.writeLock().unlock();
        }

    }

    public static void permission(@NotNull Level level, @NotNull String message) {
        permission(level, message, Caller.create(3, false));
    }

    public static void timing(@NotNull String operation, @NotNull Timer timer) {
        timing(Level.INFO, operation, timer, Caller.create());
    }

    @ApiStatus.Internal
    public static void timing(@NotNull Level level, @NotNull String operation, @NotNull Timer timer, @Nullable Caller caller) {
        LOCK.writeLock().lock();
        try {
            Record recordEntry;
            if (DISABLE_LOCATION_RECORDING) {
                recordEntry = new Record(level, Type.TIMING, operation + " (cost " + timer.getPassedTime() + " ms)", null);
            } else {
                recordEntry = new Record(level, Type.TIMING, operation + " (cost " + timer.getPassedTime() + " ms)", caller);
            }
            LOGGER_BUFFER.offer(recordEntry);
            debugStdOutputs(recordEntry);
        } finally {
            LOCK.writeLock().unlock();
        }

    }

    public static void transaction(@NotNull String message) {
        transaction(Level.INFO, message, Caller.create());
    }

    @ApiStatus.Internal
    public static void transaction(@NotNull Level level, @NotNull String message, @Nullable Caller caller) {
        LOCK.writeLock().lock();
        try {
            Record recordEntry;
            if (DISABLE_LOCATION_RECORDING) {
                recordEntry = new Record(level, Type.TRANSACTION, message, null);
            } else {
                recordEntry = new Record(level, Type.TRANSACTION, message, caller);
            }
            LOGGER_BUFFER.offer(recordEntry);
            debugStdOutputs(recordEntry);
        } finally {
            LOCK.writeLock().unlock();
        }
    }

    public static void transaction(@NotNull Level level, @NotNull String message) {
        transaction(level, message, Caller.create());
    }

    public enum Type {
        DEBUG,
        CRON,
        TRANSACTION,
        TIMING,
        PERFORMANCE,
        PRIVACY,
        PERMISSION
    }

    @Getter
    @EqualsAndHashCode
    public static class Record {
        private final long timestamp = System.currentTimeMillis();
        @NotNull
        private final Level level;
        @NotNull
        private final Type type;
        @NotNull
        private final String message;
        @Nullable
        private final Caller caller;

        public Record(@NotNull Level level, @NotNull Type type, @NotNull String message, @Nullable Caller caller) {
            this.level = level;
            this.type = type;
            this.message = message;
            this.caller = caller;
        }

        public CompletableFuture<String> generate() {
            return CompletableFuture.supplyAsync(() -> {
                StringBuilder sb = new StringBuilder();
                Log.Caller caller;
                if (this.caller == null) {
                    caller = new Caller("<NO RECORDING>", "<NO RECORDING>", "<NO RECORDING>", -1);
                } else {
                    caller = this.caller;
                }
                String simpleClassName = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
                sb.append("[");
                sb.append(caller.getThreadName());
                sb.append("/");
                sb.append(this.getLevel().getName());
                sb.append("]");
                sb.append(" ");
                sb.append("(");
                sb.append(simpleClassName).append("#").append(caller.getMethodName()).append(":").append(caller.getLineNumber());
                sb.append(")");
                sb.append(" ");
                sb.append(this.getMessage());
                return sb.toString();
            }, QuickExecutor.getCommonExecutor());
        }

        @Override
        public String toString() {
            return generate().join();
        }
    }

    @Data
    public final static class Caller {
        @NotNull
        private final String threadName;
        @NotNull
        private final String className;
        @NotNull
        private final String methodName;
        private final int lineNumber;

        public Caller(@NotNull String threadName, @NotNull String className, @NotNull String methodName, int lineNumber) {
            this.threadName = threadName;
            this.className = className;
            this.methodName = methodName;
            this.lineNumber = lineNumber;
        }

        @NotNull
        public static Caller create() {
            return create(3, false);
        }

        @NotNull
        public static Caller createSync() {
            return create(3, false);
        }

        @NotNull
        public static Caller createSync(boolean force) {
            return create(3, force);
        }

        @NotNull
        public static Caller create(int steps, boolean force) {
            if (!force) {
                if ("true".equalsIgnoreCase(System.getProperty("quickshop-hikari-disable-debug-logger"))) {
                    return new Caller("<DISABLED>", "<DISABLED>", "<DISABLED>", -1);
                }
            }
            return STACK_WALKER.walk(stream -> stream.skip(steps).findFirst()
                .map(frame -> {
                    String threadName = Thread.currentThread().getName();
                    String className = frame.getClassName();
                    String methodName = frame.getMethodName();
                    int codeLine = frame.getLineNumber();
                    return new Caller(threadName, className, methodName, codeLine);
                })
                .orElseGet(() -> new Caller("<INVALID>", "<INVALID>", "<INVALID>", -1)));
        }
    }

}
