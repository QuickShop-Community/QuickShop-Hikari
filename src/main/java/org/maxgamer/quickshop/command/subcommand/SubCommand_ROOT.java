/*
 * This file is a part of project QuickShop, the name is SubCommand_ROOT.java
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

package org.maxgamer.quickshop.command.subcommand;

import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandContainer;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class SubCommand_ROOT implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        new SubCommand_Help(plugin).onCommand(sender, commandLabel, cmdArg);
    }

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] strings) {
        final List<String> candidate = new ArrayList<>();

        for (CommandContainer container : plugin.getCommandManager().getRegisteredCommands()) {
            if (!container.getPrefix().startsWith(strings[0])
                    && !container.getPrefix().equals(strings[0])) {
                continue;
            }

            final List<String> requirePermissions = container.getPermissions();

            if (requirePermissions != null) {
                for (String requirePermission : requirePermissions) {
                    if (requirePermission != null
                            && !requirePermission.isEmpty()
                            && !QuickShop.getPermissionManager().hasPermission(sender, requirePermission)) {
                        Util.debugLog(
                                "Player "
                                        + sender.getName()
                                        + " is trying to tab-complete the command: "
                                        + commandLabel
                                        + ", but doesn't have the permission: "
                                        + requirePermission);
                        return Collections.emptyList();
                    }
                }
            }

            if (!container.isHidden() && !(container.isDisabled() || (container.getDisabledSupplier() != null && container.getDisabledSupplier().get()))) {
                candidate.add(container.getPrefix());
            }
        }

        return candidate;
    }

}
