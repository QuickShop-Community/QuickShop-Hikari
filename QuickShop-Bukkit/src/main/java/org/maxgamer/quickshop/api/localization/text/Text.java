package org.maxgamer.quickshop.api.localization.text;

import org.jetbrains.annotations.NotNull;

public interface Text {
    /**
     * Getting the text that use specify locale
     *
     * @param locale The minecraft locale code (like en_us)
     * @return The text
     */
    @NotNull String forLocale(@NotNull String locale);

    /**
     * Getting the text for player locale
     *
     * @return Getting the text for player locale
     */
    @NotNull String forLocale();

    /**
     * Send text to the player
     */
    void send();
}
