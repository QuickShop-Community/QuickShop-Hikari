package com.ghostchu.quickshop.api.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Throw when no permission provider founded.
 */
@AllArgsConstructor
public class ProviderIsEmptyException extends RuntimeException {
    @Getter
    @Setter
    private String providerName;

}
