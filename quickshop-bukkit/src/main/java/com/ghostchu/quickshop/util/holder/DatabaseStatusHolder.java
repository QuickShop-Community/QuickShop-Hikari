package com.ghostchu.quickshop.util.holder;

import com.ghostchu.quickshop.database.bean.IsolatedScanResult;

import java.util.Objects;

public class DatabaseStatusHolder {

  private Status status;
  private IsolatedScanResult<Long> dataIds;
  private IsolatedScanResult<Long> shopIds;
  private long reportGeneratedAt;

  public DatabaseStatusHolder(final Status status, final IsolatedScanResult<Long> dataIds, final IsolatedScanResult<Long> shopIds, final long reportGeneratedAt) {

    this.status = status;
    this.dataIds = dataIds;
    this.shopIds = shopIds;
    this.reportGeneratedAt = reportGeneratedAt;
  }

  public enum Status {
    GOOD, MAINTENANCE_REQUIRED;
  }

  public Status getStatus() {

    return this.status;
  }

  public IsolatedScanResult<Long> getDataIds() {

    return this.dataIds;
  }

  public IsolatedScanResult<Long> getShopIds() {

    return this.shopIds;
  }

  public long getReportGeneratedAt() {

    return this.reportGeneratedAt;
  }

  public void setStatus(final Status status) {

    this.status = status;
  }

  public void setDataIds(final IsolatedScanResult<Long> dataIds) {

    this.dataIds = dataIds;
  }

  public void setShopIds(final IsolatedScanResult<Long> shopIds) {

    this.shopIds = shopIds;
  }

  public void setReportGeneratedAt(final long reportGeneratedAt) {

    this.reportGeneratedAt = reportGeneratedAt;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof DatabaseStatusHolder)) return false;
    final DatabaseStatusHolder other = (DatabaseStatusHolder)o;
    return this.getReportGeneratedAt() == other.getReportGeneratedAt()
           && Objects.equals(this.getStatus(), other.getStatus())
           && Objects.equals(this.getDataIds(), other.getDataIds())
           && Objects.equals(this.getShopIds(), other.getShopIds());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getReportGeneratedAt(), this.getStatus(), this.getDataIds(), this.getShopIds());
  }

  @Override
  public String toString() {

    return "DatabaseStatusHolder(status=" + this.getStatus() + ", dataIds=" + this.getDataIds() + ", shopIds=" + this.getShopIds() + ", reportGeneratedAt=" + this.getReportGeneratedAt() + ")";
  }
}



