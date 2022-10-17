package com.ghostchu.quickshop.api.localization.text;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * The TextManager that allow create user's locale specified message.
 */
public interface TextManager {
    @NotNull Component[] convert(@Nullable Object... args);

    @NotNull ProxiedLocale findRelativeLanguages(@Nullable String langCode);

    @NotNull ProxiedLocale findRelativeLanguages(@Nullable CommandSender sender);

    @NotNull ProxiedLocale findRelativeLanguages(@Nullable UUID sender);

    /**
     * Return the set of available Languages
     *
     * @return the set of available Languages
     */
    default List<String> getAvailableLocales() {
        return getAvailableLanguages();
    }

    /**
     * Return the set of available Languages
     *
     * @return the set of available Languages
     */
    List<String> getAvailableLanguages();

    /**
     * Gets specific locale status
     *
     * @param locale The locale
     * @param regex  The regexes
     * @return The locale enabled status
     */
    boolean localeEnabled(@NotNull String locale, @NotNull List<String> regex);

    /**
     * Getting the translation with path with default locale
     *
     * @param path THe path
     * @param args The arguments
     * @return The text object
     */
    @NotNull
    Text of(@NotNull String path, @NotNull Object... args);

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The sender
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @NotNull
    Text of(@Nullable CommandSender sender, @NotNull String path, @Nullable Object... args);

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The player unique id
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @NotNull
    Text of(@Nullable UUID sender, @NotNull String path, @Nullable Object... args);

    /**
     * Getting the translation with path with default locale (if available)
     *
     * @param path The path
     * @param args The arguments
     * @return The text object
     */
    @NotNull
    TextList ofList(@NotNull String path, @Nullable Object... args);

    /**
     * Getting the translation with path  with player's locale (if available)
     *
     * @param sender The player unique id
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @NotNull
    TextList ofList(@Nullable UUID sender, @NotNull String path, @Nullable Object... args);

    /**
     * Getting the translation with path with player's locale (if available)
     *
     * @param sender The player
     * @param path   The path
     * @param args   The arguments
     * @return The text object
     */
    @NotNull
    TextList ofList(@Nullable CommandSender sender, @NotNull String path, @Nullable Object... args);

    /**
     * Register the language phrase to QuickShop text manager in runtime.
     *
     * @param locale Target locale
     * @param path   The language key path
     * @param text   The language text
     */
    void register(@NotNull String locale, @NotNull String path, @NotNull String text);
}
