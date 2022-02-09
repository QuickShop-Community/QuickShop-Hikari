package org.maxgamer.quickshop.api.integration;

import org.jetbrains.annotations.ApiStatus;

/**
 * @deprecated Please listen the (ShopCreateEvent and ShopPurchaseEvent) events to instead
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public class InvalidIntegratedPluginClassException extends IllegalArgumentException {
    public InvalidIntegratedPluginClassException() {
        super();
    }

    public InvalidIntegratedPluginClassException(String s) {
        super(s);
    }

    public InvalidIntegratedPluginClassException(String message, Throwable cause) {
        super(message, cause);
    }
}
