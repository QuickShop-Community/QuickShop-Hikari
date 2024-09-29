package com.ghostchu.quickshop.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class ExpiringSet<T> {

  private final Cache<T, Long> cache;
  private final long lifetime;

  public ExpiringSet(final long lifetime, final TimeUnit timeUnit) {

    this.cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(lifetime, timeUnit).build();
    this.lifetime = timeUnit.toMillis(lifetime);
  }

  public void add(final T item) {

    this.cache.put(item, System.currentTimeMillis() + this.lifetime);
  }

  public boolean contains(final T item) {

    final Long timeout = this.cache.getIfPresent(item);
    return timeout != null && timeout > System.currentTimeMillis();
  }

  public void remove(final T item) {

    this.cache.invalidate(item);
  }

  public long size() {

    return this.cache.size();
  }
}
