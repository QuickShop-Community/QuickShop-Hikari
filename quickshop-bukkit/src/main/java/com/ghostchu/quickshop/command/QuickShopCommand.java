package com.ghostchu.quickshop.command;

import com.ghostchu.quickshop.api.command.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuickShopCommand extends BukkitCommand {
    private final CommandManager manager;

    public QuickShopCommand(@NotNull String name, CommandManager commandManager, @NotNull List<String> aliases) {
        super(name, "QuickShop command", "/qs", aliases);
        this.manager = commandManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return this.manager.onCommand(sender, this, commandLabel, args);
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> items = this.manager.onTabComplete(sender, this, alias, args);
        if (items == null) {
            items = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return items;
    }
}
