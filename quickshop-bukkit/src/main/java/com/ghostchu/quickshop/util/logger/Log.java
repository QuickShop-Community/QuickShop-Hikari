/*
 *  This file is a part of project QuickShop, the name is QuickLogger.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.util.logger;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.Util;
import com.google.common.collect.EvictingQueue;
import lombok.AllArgsConstructor;
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
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

public class Log {
    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();
    private static final int bufferSize = 2500 * Type.values().length;
    private static final Queue<Record> loggerBuffer = EvictingQueue.create(bufferSize);

    private static final boolean disableLocationRecording;

    static {
        // Cannot replace with Util since it depend on this class
        disableLocationRecording = Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.util.logger."));
    }


    public static void debug(@NotNull String message) {
        debug(Level.INFO, message, Caller.create());
    }

    public static void cron(@NotNull String message) {
        cron(Level.INFO, message, Caller.create());
    }

    public static void transaction(@NotNull String message) {
        transaction(Level.INFO, message, Caller.create());
    }

    public static void permission(@NotNull String message) {
        permission(Level.INFO, message, Caller.create(3));
    }


    public static void debug(@NotNull Level level, @NotNull String message) {
        debug(level, message, Caller.create());
    }

    public static void cron(@NotNull Level level, @NotNull String message) {
        cron(level, message, Caller.create());
    }

    public static void transaction(@NotNull Level level, @NotNull String message) {
        transaction(level, message, Caller.create());
    }

    public static void permission(@NotNull Level level, @NotNull String message) {
        permission(level, message, Caller.create(3));
    }

    @ApiStatus.Internal
    public static void debug(@NotNull Level level, @NotNull String message, @Nullable Caller caller) {
        LOCK.writeLock().lock();
        Record record;
        if (disableLocationRecording)
            record = new Record(level, Type.DEBUG, message, null);
        else
            record = new Record(level, Type.DEBUG, message, caller);
        loggerBuffer.offer(record);
        debugStdOutputs(record);
        LOCK.writeLock().unlock();
    }

    @ApiStatus.Internal
    public static void cron(@NotNull Level level, @NotNull String message, @Nullable Caller caller) {
        LOCK.writeLock().lock();
        Record record;
        if (disableLocationRecording)
            record = new Record(level, Type.CRON, message, null);
        else
            record = new Record(level, Type.CRON, message, caller);
        loggerBuffer.offer(record);
        debugStdOutputs(record);
        LOCK.writeLock().unlock();
    }

    @ApiStatus.Internal
    public static void transaction(@NotNull Level level, @NotNull String message, @Nullable Caller caller) {
        LOCK.writeLock().lock();
        Record record;
        if (disableLocationRecording)
            record = new Record(level, Type.TRANSACTION, message, null);
        else
            record = new Record(level, Type.TRANSACTION, message, caller);
        loggerBuffer.offer(record);
        debugStdOutputs(record);
        LOCK.writeLock().unlock();
    }

    @ApiStatus.Internal
    public static void permission(@NotNull Level level, @NotNull String message, @Nullable Caller caller) {
        LOCK.writeLock().lock();
        Record record;
        if (disableLocationRecording)
            record = new Record(level, Type.PERMISSION, message, null);
        else
            record = new Record(level, Type.PERMISSION, message, caller);
        loggerBuffer.offer(record);
        debugStdOutputs(record);
        LOCK.writeLock().unlock();
    }

    private static void debugStdOutputs(Record record) {
        if (Util.isDevMode()) {
            QuickShop.getInstance().getLogger().info("[DEBUG] " + record.toString());
        }
    }

    @NotNull
    public static List<Record> fetchLogs() {
        LOCK.readLock().lock();
        List<Record> records = new ArrayList<>(loggerBuffer);
        LOCK.readLock().unlock();
        return records;
    }

    @NotNull
    public static List<Record> fetchLogs(@NotNull Type type) {
        LOCK.readLock().lock();
        List<Record> records = loggerBuffer.stream().filter(record -> record.getType() == type).toList();
        LOCK.readLock().unlock();
        return records;
    }

    @NotNull
    public static List<Record> fetchLogsLevel(@NotNull Type type, @NotNull Level level) {
        LOCK.readLock().lock();
        List<Record> records = loggerBuffer.stream().filter(record -> record.getType() == type && record.getLevel() == level).toList();
        LOCK.readLock().unlock();
        return records;
    }

    @NotNull
    public static List<Record> fetchLogsExclude(@NotNull Type... excludes) {
        LOCK.readLock().lock();
        List<Record> records = new ArrayList<>();
        for (Record record : loggerBuffer) {
            if (ArrayUtils.contains(excludes, record.getType())) {
                continue;
            }
            records.add(record);
        }
        LOCK.readLock().unlock();
        return records;
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
        @Nullable
        private String toStringCache;

        public Record(@NotNull Level level, @NotNull Type type, @NotNull String message, @Nullable Caller caller) {
            this.level = level;
            this.type = type;
            this.message = message;
            this.caller = caller;
        }

        @Override
        public String toString() {
            if (toStringCache != null)
                return toStringCache;
            StringBuilder sb = new StringBuilder();
            Log.Caller caller = this.getCaller();

            //noinspection IfStatementWithIdenticalBranches
            if (caller != null) {
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
            } else {
                sb.append("[");
                sb.append(this.getLevel().getName());
                sb.append("]");
                sb.append(" ");
            }
            sb.append(this.getMessage());
            toStringCache = sb.toString();
            return toStringCache;
        }

    }

    @AllArgsConstructor
    @Data
    public static class Caller {
        @NotNull
        private final static StackWalker stackWalker = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), 3);
        @NotNull
        private final String threadName;
        @NotNull
        private final String className;
        @NotNull
        private final String methodName;
        private final int lineNumber;

        @NotNull
        public static Caller create() {
            return create(3);
        }

        @NotNull
        public static Cal
        String threadName = Thread.currentThread().getName();
        String className = frame.getClassName();
        String methodName = frame.getMethodName();
        int codeLine = frame.getLineNumber();
            return new

        Caller(threadName, className, methodName, codeLine);
    }
    }


    public enum Type {
        DEBUG,
        CRON,
        TRANSACTION,
        PERMISSION
    }

}
