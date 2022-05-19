/*
 *  This file is a part of project QuickShop, the name is SubCommand_About.java
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
import com.ghostchu.quickshop.util.ItemMarker;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@AllArgsConstructor
public class SubCommand_Lookup implements CommandHandler<Player> {
    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender.getInventory().getItemInMainHand().getType().isAir()) {
            plugin.text().of(sender, "no-anythings-in-your-hand").send();
            return;
        }
        ItemStack item = sender.getInventory().getItemInMainHand();
        if (cmdArg.length < 2) {
            plugin.text().of(sender, "command-incorrect", "/qs lookup <create/remove/test> <name>").send();
            return;
        }

        if (cmdArg.length == 2) {
            if ("test".equals(cmdArg[1].toLowerCase(Locale.ROOT))) {
                String name = plugin.getItemMarker().get(item);
                if (name == null) {
                    plugin.text().of(sender, "lookup-item-test-not-found").send();
                } else {
                    plugin.text().of(sender, "lookup-item-test-found", name).send();
                }
                return;
            }
            plugin.text().of(sender, "command-incorrect", "/qs lookup <create/remove/test> <name>").send();
            return;
        }
        switch (cmdArg[1].toLowerCase(Locale.ROOT)) {
            case "create" -> {
                ItemMarker.OperationResult result = plugin.getItemMarker().save(cmdArg[2], item);
                switch (result) {
                    case SUCCESS -> plugin.text().of(sender, "lookup-item-created", cmdArg[0]).send();
                    case REGEXP_FAILURE ->
                            plugin.text().of(sender, "lookup-item-name-regex", plugin.getItemMarker().getNameRegExp(), cmdArg[0]).send();
                    case NAME_CONFLICT -> plugin.text().of(sender, "lookup-item-exists", cmdArg[0]).send();
                    default -> plugin.text().of(sender, "internal-error", cmdArg[0]).send();
                }
            }
            case "remove" -> {
                ItemMarker.OperationResult result = plugin.getItemMarker().remove(cmdArg[2]);
                switch (result) {
                    case SUCCESS -> plugin.text().of(sender, "lookup-item-removed", cmdArg[0]).send();
                    case NOT_EXISTS -> plugin.text().of(sender, "lookup-item-not-found", cmdArg[0]).send();
                    default -> plugin.text().of(sender, "internal-error", cmdArg[0]).send();
                }
            }
            default -> plugin.text().of(sender, "command-incorrect", "/qs lookup <create/remove/test> <name>").send();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length == 1) {
            return Arrays.asList("create", "remove", "test");
        }
        if (cmdArg.length > 1) {
            if (cmdArg[0].equalsIgnoreCase("remove")) {
                return plugin.getItemMarker().getRegisteredItems();
            }
        }
        return Collections.emptyList();
    }
}
