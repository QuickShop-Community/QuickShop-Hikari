package com.ghostchu.quickshop.util;

import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ProgressMonitor<T> implements Iterable<T> {

  private final Collection<T> elements;
  private final Consumer<Triple<Long, Long, T>> callback;

  public ProgressMonitor(@NotNull final Collection<T> elements, final Consumer<Triple<Long, Long, T>> callback) {

    this.elements = elements;
    this.callback = callback;
  }

  public ProgressMonitor(@NotNull final T[] elements, final Consumer<Triple<Long, Long, T>> callback) {

    this.elements = Arrays.asList(elements);
    this.callback = callback;
  }

  @NotNull
  @Override
  public Iterator<T> iterator() {

    final Iterator<T> parent = elements.iterator();
    final long total = elements.size();
    final AtomicLong count = new AtomicLong(0L);
    return new Iterator<>() {
      @Override
      public boolean hasNext() {

        return parent.hasNext();
      }

      @Override
      public T next() {

        final T dat = parent.next();
        callback.accept(Triple.of(count.incrementAndGet(), total, dat));
        return dat;
      }
    };
  }
}
