/*
 * This file is a part of project QuickShop, the name is SubCommand_SilentBase.java
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

package org.maxgamer.quickshop.command.subcommand.silent;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public abstract class SubCommand_SilentBase implements CommandHandler<Player> {
    protected final QuickShop plugin;

    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length != 1) {
            Util.debugLog("Exception on command! Canceling!");
            return;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(cmdArg[0]);
        } catch (IllegalArgumentException e) {
            //Not valid, return for doing nothing
            return;
        }

        Shop shop = plugin.getShopManager().getShopFromRuntimeRandomUniqueId(uuid);
        if (shop != null) {
            doSilentCommand(sender, shop, cmdArg);
        } else {
            plugin.text().of(sender, "not-looking-at-shop").send();
        }
    }

    protected abstract void doSilentCommand(Player sender, @NotNull Shop shop, @NotNull String[] cmdArg);

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }
}
