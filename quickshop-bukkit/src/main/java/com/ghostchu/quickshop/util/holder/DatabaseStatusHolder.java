package com.ghostchu.quickshop.util.holder;

import com.ghostchu.quickshop.database.bean.IsolatedScanResult;
import lombok.Data;

@Data
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
    GOOD,
    MAINTENANCE_REQUIRED,
  }
}



