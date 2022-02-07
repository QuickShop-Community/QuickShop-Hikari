/*
 * This file is a part of project QuickShop, the name is LocalizedMessagePair.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.localization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;

public class LocalizedMessagePair {
    private static final QuickShop plugin = QuickShop.getInstance();
    @NotNull
    private final String key;
    @NotNull
    private final String[] args;

    public LocalizedMessagePair(@NotNull String key, @NotNull String... args) {
        this.key = key;
        this.args = args;
    }

    public static LocalizedMessagePair of(@NotNull String key, @NotNull String... args) {
        return new LocalizedMessagePair(key, args);
    }

    public String getLocalizedMessage(@Nullable String locale) {
        if (locale == null) {
            return plugin.getTextManager().of(key, (Object[]) args).forLocale();
        } else {
            return plugin.getTextManager().of(key, (Object[]) args).forLocale(locale);
        }
    }
}
