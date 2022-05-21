/*
 *  This file is a part of project QuickShop, the name is SubCommand_Reload.java
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

package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.ReloadableContainer;
import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
public class SubCommand_Reload implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        plugin.text().of(sender, "command.reloading").send();
        plugin.reloadConfig();
        Map<ReloadableContainer, ReloadResult> container = plugin.getReloadManager().reload();
        sender.sendMessage(ChatColor.GOLD + "Reloaded " + container.size() + " modules.");
        if (Util.isDevMode()) {
            Map<ReloadStatus, AtomicInteger> status = new HashMap<>();
            for (ReloadResult value : container.values()) {
                AtomicInteger integer = status.computeIfAbsent(value.getStatus(), k -> new AtomicInteger(0));
                integer.incrementAndGet();
            }
            status.forEach((key, value) -> sender.sendMessage(ChatColor.GOLD + Util.firstUppercase(key.name()) + ": " + value.get()));
        }
    }
}
