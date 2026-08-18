package com.ghostchu.quickshop.database.bean;

import java.util.List;
import java.util.Objects;

public class IsolatedScanResult<T> {

  private final List<T> total;
  private final List<T> isolated;

  public IsolatedScanResult(final List<T> total, final List<T> isolated) {

    this.total = total;
    this.isolated = isolated;
  }

  public List<T> getTotal() {

    return this.total;
  }

  public List<T> getIsolated() {

    return this.isolated;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof IsolatedScanResult)) return false;
    final IsolatedScanResult<?> other = (IsolatedScanResult<?>)o;
    return Objects.equals(this.getTotal(), other.getTotal())
           && Objects.equals(this.getIsolated(), other.getIsolated());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getTotal(), this.getIsolated());
  }

  @Override
  public String toString() {

    return "IsolatedScanResult(total=" + this.getTotal() + ", isolated=" + this.getIsolated() + ")";
  }
}
