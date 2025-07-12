package com.ghostchu.quickshop.api.permission;

import lombok.Getter;

/**
 * Throw when no permission provider founded.
 */
@Getter
public class ProviderIsEmptyException extends RuntimeException {

  private final String providerName;

  public ProviderIsEmptyException(final String providerName) {

    this.providerName = providerName;
  }
}
