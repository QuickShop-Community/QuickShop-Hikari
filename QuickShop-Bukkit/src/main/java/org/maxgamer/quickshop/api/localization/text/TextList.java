package org.maxgamer.quickshop.api.localization.text;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TextList {

    /**
     * Getting the text that use specify locale
     *
     * @param locale The minecraft locale code (like en_us)
     * @return The text
     */
    @NotNull List<String> forLocale(@NotNull String locale);

    /**
     * Getting the text for player locale
     *
     * @return Getting the text for player locale
     */
    @NotNull List<String> forLocale();

    /**
     * Send text to the player
     */
    void send();
}
