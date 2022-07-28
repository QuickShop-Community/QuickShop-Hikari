package com.ghostchu.quickshop.api.localization.text;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * The Texts of the translation.
 */
public interface Text {
    /**
     * Getting the text that use specify locale
     *
     * @param locale The minecraft locale code (like en_us)
     * @return The text
     */
    @NotNull Component forLocale(@NotNull String locale);

    /**
     * Getting the text for player locale
     *
     * @return Getting the text for player locale
     */
    @NotNull Component forLocale();

    /**
     * Getting this text is exists in the translation file
     *
     * @return true if this text is exists in the translation file
     */
    boolean isPresent();

    /**
     * Send text to the player
     */
    void send();
}
