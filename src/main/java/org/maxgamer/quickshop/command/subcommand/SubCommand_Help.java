/*
 * This file is a part of project QuickShop, the name is SubCommand_Help.java
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

import java.util.List;

@AllArgsConstructor
public class SubCommand_Help implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        sendHelp(sender, commandLabel);
    }


    private void sendHelp(@NotNull CommandSender s, @NotNull String commandLabel) {
        plugin.text().of(s, "command.description.title").send();

        commandPrintingLoop:
        for (CommandContainer container : plugin.getCommandManager().getRegisteredCommands()) {
            if (!container.isHidden()) {
                boolean passed = false;
                //selectivePermissions
                final List<String> selectivePermissions = container.getSelectivePermissions();
                if (selectivePermissions != null) {
                    for (String selectivePermission : container.getSelectivePermissions()) {
                        if (selectivePermission != null && !selectivePermission.isEmpty()) {
                            if (QuickShop.getPermissionManager().hasPermission(s, selectivePermission)) {
                                passed = true;
                                break;
                            }
                        }
                    }
                }
                //requirePermissions
                final List<String> requirePermissions = container.getPermissions();
                if (requirePermissions != null) {
                    for (String requirePermission : requirePermissions) {
                        if (requirePermission != null && !requirePermission.isEmpty() && !QuickShop.getPermissionManager().hasPermission(s, requirePermission)) {
                            continue commandPrintingLoop;
                        }
                    }
                    passed = true;
                }
                if (!passed) {
                    continue;
                }
                String commandDesc = plugin.text().of(s, "command.description." + container.getPrefix()).forLocale();
                if (container.getDescription() != null) {
                    commandDesc = container.getDescription();
                    if (commandDesc == null) {
                        commandDesc = "Error: Subcommand " + container.getPrefix() + " # " + container.getClass().getCanonicalName() + " doesn't register the correct help description.";
                    }
                }
                if (container.isDisabled() || (container.getDisabledSupplier() != null && container.getDisabledSupplier().get())) {
                    if (QuickShop.getPermissionManager().hasPermission(s, "quickshop.showdisabled")) {
                        plugin.text().of(s, "command.format", commandLabel, container.getPrefix(), container.getDisableText(s)).send();
                    }
                } else {
                    plugin.text().of(s, "command.format", commandLabel, container.getPrefix(), commandDesc).send();
                }
            }
        }
    }

}
