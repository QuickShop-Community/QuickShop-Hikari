package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.api.shop.PriceLimiterCheckResult;
import com.ghostchu.quickshop.api.shop.PriceLimiterStatus;

import java.util.Objects;

public class SimplePriceLimiterCheckResult implements PriceLimiterCheckResult {

  PriceLimiterStatus status;
  double min;
  double max;

  public SimplePriceLimiterCheckResult(final PriceLimiterStatus status, final double min, final double max) {

    this.status = status;
    this.min = min;
    this.max = max;
  }

  public PriceLimiterStatus getStatus() {

    return this.status;
  }

  public double getMin() {

    return this.min;
  }

  public double getMax() {

    return this.max;
  }

  public void setStatus(final PriceLimiterStatus status) {

    this.status = status;
  }

  public void setMin(final double min) {

    this.min = min;
  }

  public void setMax(final double max) {

    this.max = max;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof SimplePriceLimiterCheckResult)) return false;
    final SimplePriceLimiterCheckResult other = (SimplePriceLimiterCheckResult)o;
    return Double.compare(this.getMin(), other.getMin()) == 0
           && Double.compare(this.getMax(), other.getMax()) == 0
           && Objects.equals(this.getStatus(), other.getStatus());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getMin(), this.getMax(), this.getStatus());
  }

  @Override
  public String toString() {

    return "SimplePriceLimiterCheckResult(status=" + this.getStatus() + ", min=" + this.getMin() + ", max=" + this.getMax() + ")";
  }
}
