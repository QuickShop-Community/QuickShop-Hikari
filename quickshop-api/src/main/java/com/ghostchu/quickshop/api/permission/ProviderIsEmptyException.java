package com.ghostchu.quickshop.api.permission;

import lombok.Getter;

/**
 * Throw when no permission provider founded.
 */
public class ProviderIsEmptyException extends RuntimeException {

  @Getter
  private final String providerName;

  public ProviderIsEmptyException(final String providerName) {

    this.providerName = providerName;
  }
}
