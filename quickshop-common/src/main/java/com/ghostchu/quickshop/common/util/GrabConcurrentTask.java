package com.ghostchu.quickshop.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class GrabConcurrentTask<T> {

  private final LinkedBlockingDeque<Optional<T>> deque = new LinkedBlockingDeque<>();
  private final List<Supplier<T>> suppliers;
  private final ExecutorService service;

  @SafeVarargs
  public GrabConcurrentTask(@NotNull final ExecutorService service, @NotNull final Supplier<T>... suppliers) {

    this.service = service;
    this.suppliers = new ArrayList<>(List.of(suppliers));
  }

  public void addSupplier(@NotNull final Supplier<T> element) {

    suppliers.add(element);
  }

  @Nullable
  public T invokeAll(final String executeName, final long timeout, @NotNull final TimeUnit unit, @Nullable Predicate<T> condition) throws InterruptedException {
    // Submit all tasks into executor
    for(final Supplier<T> supplier : suppliers) {
      service.submit(new GrabConcurrentExecutor<>(executeName, deque, supplier));
    }
    if(condition == null) {
      condition = t->true;
    }
    final Instant instant = Instant.now();
    final Instant timeoutInstant = instant.plus(timeout, unit.toChronoUnit());
    final long timeoutEpochSecond = timeoutInstant.getEpochSecond();
    T value;
    do {
      // loop timed out
      final long loopAllowedWaitingTime = timeoutEpochSecond - Instant.now().getEpochSecond();
      if(loopAllowedWaitingTime <= 0) {
        return null;
      }
      final Optional<T> element = deque.poll(loopAllowedWaitingTime, TimeUnit.SECONDS);
      //noinspection OptionalAssignedToNull
      if(element == null) {
        // poll timed out
        return null;
      }
      value = element.orElse(null);
    } while(!condition.test(value));
    return value;
  }

  static class GrabConcurrentExecutor<T> implements Runnable {

    public final LinkedBlockingDeque<Optional<T>> targetDeque;
    private final Supplier<T> supplier;
    private final String executeName;

    public GrabConcurrentExecutor(@NotNull final String executeName, @NotNull final LinkedBlockingDeque<Optional<T>> targetDeque, @NotNull final Supplier<T> supplier) {

      this.executeName = executeName;
      this.targetDeque = targetDeque;
      this.supplier = supplier;
    }

    @Override
    public void run() {

      Optional<T> value = Optional.empty();
      try {
        value = Optional.ofNullable(supplier.get());
      } catch(Throwable e) {
        e.printStackTrace();
      } finally {
        targetDeque.offer(value);
      }
    }

    @Override
    public String toString() {

      return "GrabConcurrentExecutor{" +
             "targetDeque=" + targetDeque +
             ", supplier=" + supplier +
             ", executeName='" + executeName + '\'' +
             '}';
    }
  }
}
