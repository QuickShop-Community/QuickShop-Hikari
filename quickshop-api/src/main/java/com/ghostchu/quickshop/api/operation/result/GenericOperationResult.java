package com.ghostchu.quickshop.api.operation.result;

import com.ghostchu.quickshop.api.operation.OperationResult;

public class GenericOperationResult implements OperationResult<Boolean> {

  private final boolean success;

  public GenericOperationResult(final boolean success) {

    this.success = success;
  }

  @Override
  public boolean success() {

    return this.success;
  }

  @Override
  public Boolean result() {

    return this.success;
  }
}