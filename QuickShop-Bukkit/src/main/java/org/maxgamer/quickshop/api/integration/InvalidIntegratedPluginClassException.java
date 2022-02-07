package org.maxgamer.quickshop.api.integration;

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
