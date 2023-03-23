package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadableContainer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SubCommand_Reload implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    public SubCommand_Reload(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        plugin.text().of(sender, "command.reloading").send();
        plugin.getJavaPlugin().reloadConfig();
        Map<ReloadableContainer, ReloadResult> container = plugin.getReloadManager().reload();
        sender.sendMessage(ChatColor.GOLD + "Reloaded " + container.size() + " modules.");
    }
}
