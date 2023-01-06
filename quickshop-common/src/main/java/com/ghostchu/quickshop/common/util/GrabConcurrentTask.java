package com.ghostchu.quickshop.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class GrabConcurrentTask<T> {
    private final LinkedBlockingDeque<Optional<T>> deque = new LinkedBlockingDeque<>();
    private final List<Supplier<T>> suppliers;

    @SafeVarargs
    public GrabConcurrentTask(@NotNull Supplier<T>... suppliers) {
        this.suppliers = new ArrayList<>(List.of(suppliers));
    }

    public void addSupplier(@NotNull Supplier<T> element) {
        suppliers.add(element);
    }

    @Nullable
    public T invokeAll(long timeout, @NotNull TimeUnit unit, @Nullable Function<@Nullable T, @NotNull Boolean> condition) throws InterruptedException {
        int counter = suppliers.size();
        for (Supplier<T> supplier : suppliers) {
            QuickExecutor.getCommonExecutor().submit(new GrabConcurrentExecutor<>(deque, supplier));
        }
        while (true) {
            Optional<T> element = deque.poll(timeout, unit);
            counter--;
            //if poll behavior timed out, we will get a null element
            //noinspection OptionalAssignedToNull
            if (element == null) {
                return null;
            }
            T value = element.orElse(null);
            if (condition != null) {
                if (condition.apply(value)) {
                    return value;
                }
            } else {
                return value;
            }
            if (counter < 1) {
                return null;
            }
        }
    }

    static class GrabConcurrentExecutor<T> implements Runnable {
        public final LinkedBlockingDeque<Optional<T>> targetDeque;
        private final Supplier<T> supplier;

        public GrabConcurrentExecutor(@NotNull LinkedBlockingDeque<Optional<T>> targetDeque, @NotNull Supplier<T> supplier) {
            this.targetDeque = targetDeque;
            this.supplier = supplier;
        }

        @Override
        public void run() {
            try {
                targetDeque.put(Optional.ofNullable(supplier.get()));
            } catch (Throwable e) {
                e.printStackTrace();
                targetDeque.offer(Optional.empty());
            }
        }
    }
}
