/*
 * This file is a part of project QuickShop, the name is PlaceHolderApiProcessor.java
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

package org.maxgamer.quickshop.localization.text.postprocessing.impl;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.localization.text.postprocessor.PostProcessor;

public class PlaceHolderApiProcessor implements PostProcessor {
    @Override
    public @NotNull String process(@NotNull String text, @Nullable CommandSender sender, Object... args) {
        if (sender instanceof OfflinePlayer) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceHolderAPI")) {
                return PlaceholderAPI.setPlaceholders((OfflinePlayer) sender, text);
            }
        }
        return text;
    }

}
