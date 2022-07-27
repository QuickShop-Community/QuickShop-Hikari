package com.ghostchu.quickshop.api.command;

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
     * @param container The {@link CommandContainer} to register
     * @throws IllegalStateException Will throw the error if register conflict.
     */
    void registerCmd(@NotNull CommandContainer container);

    /**
     * This is a interface to allow addons to unregister the registered/butil-in subcommand from command manager.
     *
     * @param container The {@link CommandContainer} to unregister
     */
    void unregisterCmd(@NotNull CommandContainer container);

    /**
     * This is a interface to allow addons to unregister the registered/butil-in subcommand from command manager.
     *
     * @param prefix The prefix of command need to be unregistered
     */
    void unregisterCmd(@NotNull String prefix);

    /**
     * Gets a list contains all registered commands
     *
     * @return All registered {@link CommandContainer}s.
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
