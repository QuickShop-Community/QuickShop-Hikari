package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class SubCommand_Reset implements CommandHandler<CommandSender> {

    private final QuickShop plugin;
    private final List<String> tabCompleteList = List.of("config");

    public SubCommand_Reset(QuickShop plugin) {
        this.plugin = plugin;
    }


    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() < 1) {
            plugin.text().of(sender, "command.no-type-given").send();
            return;
        }

        switch (parser.getArgs().get(0)) {
            case "config" -> {
                File config = new File(plugin.getDataFolder(), "config.yml");
                config.delete();
                plugin.getJavaPlugin().saveDefaultConfig();
                plugin.getJavaPlugin().reloadConfig();
                plugin.getReloadManager().reload();
                plugin.text().of(sender, "complete").send();
            }
            default -> plugin.text().of(sender, "command.wrong-args").send();
        }
    }

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return tabCompleteList;
    }

}
