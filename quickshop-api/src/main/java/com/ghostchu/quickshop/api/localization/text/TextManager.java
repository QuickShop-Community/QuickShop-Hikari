/*
 *  This file is a part of project QuickShop, the name is TextManager.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.api.localization.text;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * The TextManager that allow create user's locale specified message.
 */
public interface TextManager {
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
     * Return the set of available Languages
     *
     * @return the set of available Languages
     */
    List<String> getAvailableLanguages();

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
}
