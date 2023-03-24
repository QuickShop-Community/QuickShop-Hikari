package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubCommand_ROOT implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    public SubCommand_ROOT(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        new SubCommand_Help(plugin).onCommand(sender, commandLabel, parser);
    }

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        final List<String> candidate = new ArrayList<>();
        for (CommandContainer container : plugin.getCommandManager().getRegisteredCommands()) {
            if (!parser.getArgs().isEmpty() && !container.getPrefix().startsWith(parser.getArgs().get(0))) {
                continue;
            }

            final List<String> requirePermissions = container.getPermissions();

            if (requirePermissions != null) {
                for (String requirePermission : requirePermissions) {
                    if (requirePermission != null
                            && !requirePermission.isEmpty()
                            && !plugin.perm().hasPermission(sender, requirePermission)) {
                        if (Util.isDevMode()) {
                            Log.debug(
                                    "Player "
                                            + sender.getName()
                                            + " is trying to tab-complete the command: "
                                            + commandLabel
                                            + ", but doesn't have the permission: "
                                            + requirePermission);
                        }
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
