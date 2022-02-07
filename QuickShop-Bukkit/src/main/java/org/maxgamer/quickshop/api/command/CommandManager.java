/*
 * This file is a part of project QuickShop, the name is CommandManager.java
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

package org.maxgamer.quickshop.api.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The manager that managing all sub-commands that registered
 * Also performing permission checks in there.
 */
public interface CommandManager {
    /**
     * This is a interface to allow addons to register the subcommand into quickshop command manager.
     *
     * @param container The command container to register
     * @throws IllegalStateException Will throw the error if register conflict.
     */
    void registerCmd(@NotNull CommandContainer container);

    /**
     * This is a interface to allow addons to unregister the registered/butil-in subcommand from command manager.
     *
     * @param container The command container to unregister
     */
    void unregisterCmd(@NotNull CommandContainer container);

    /**
     * Gets a list contains all registered commands
     *
     * @return All registered commands.
     */
    @NotNull List<CommandContainer> getRegisteredCommands();

    boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String commandLabel,
            @NotNull String[] cmdArg);

    @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String commandLabel,
            @NotNull String[] cmdArg);
}
