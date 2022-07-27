package com.ghostchu.quickshop.api.permission;

import lombok.Getter;
import lombok.Setter;

/**
 * Throw when no permission provider founded.
 */
public class ProviderIsEmptyException extends RuntimeException {
    @Getter
    @Setter
    private String providerName;

    public ProviderIsEmptyException(String providerName) {
        this.providerName = providerName;
    }
}
