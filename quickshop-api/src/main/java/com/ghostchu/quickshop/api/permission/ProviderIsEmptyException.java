package com.ghostchu.quickshop.api.permission;

/**
 * Throw when no permission provider founded.
 */
public class ProviderIsEmptyException extends RuntimeException {

  private final String providerName;

  public ProviderIsEmptyException(final String providerName) {

    this.providerName = providerName;
  }

  public String getProviderName() {

    return this.providerName;
  }
}
