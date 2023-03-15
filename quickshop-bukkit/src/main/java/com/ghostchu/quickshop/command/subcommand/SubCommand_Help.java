package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.util.MsgUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SubCommand_Help implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    public SubCommand_Help(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        sendHelp(sender, commandLabel);
    }


    private void sendHelp(@NotNull CommandSender s, @NotNull String commandLabel) {
        plugin.text().of(s, "command.description.title").send();
        String locale = MsgUtil.getDefaultGameLanguageCode();
        if (s instanceof Player p) {
            locale = p.getLocale();
        }
        commandPrintingLoop:
        for (CommandContainer container : plugin.getCommandManager().getRegisteredCommands()) {
            if (!container.isHidden()) {
                boolean passed = false;
                //selectivePermissions
                final List<String> selectivePermissions = container.getSelectivePermissions();
                if (selectivePermissions != null && !selectivePermissions.isEmpty()) {
                    for (String selectivePermission : container.getSelectivePermissions()) {
                        if (selectivePermission != null && !selectivePermission.isEmpty()) {
                            if (plugin.perm().hasPermission(s, selectivePermission)) {
                                passed = true;
                                break;
                            }
                        }
                    }
                }
                //requirePermissions
                final List<String> requirePermissions = container.getPermissions();
                if (requirePermissions != null && !requirePermissions.isEmpty()) {
                    for (String requirePermission : requirePermissions) {
                        if (requirePermission != null && !requirePermission.isEmpty() && !plugin.perm().hasPermission(s, requirePermission)) {
                            continue commandPrintingLoop;
                        }
                    }
                    passed = true;
                }
                if (!passed) {
                    continue;
                }
                Component commandDesc = plugin.text().of(s, "command.description." + container.getPrefix()).forLocale();
                if (container.getDescription() != null) {
                    commandDesc = container.getDescription().apply(locale);
                    if (commandDesc == null) {
                        commandDesc = Component.text("Error: Subcommand " + container.getPrefix() + " # " + container.getClass().getCanonicalName() + " doesn't register the correct help description.");
                    }
                }
                if (container.isDisabled() || (container.getDisabledSupplier() != null && container.getDisabledSupplier().get())) {
                    if (plugin.perm().hasPermission(s, "quickshop.showdisabled")) {
                        plugin.text().of(s, "command.format-disabled", commandLabel, container.getPrefix(), container.getDisableText(s)).send();
                    }
                } else {
                    plugin.text().of(s, "command.format", commandLabel, container.getPrefix(), commandDesc).send();
                }
            }
        }
    }

}
