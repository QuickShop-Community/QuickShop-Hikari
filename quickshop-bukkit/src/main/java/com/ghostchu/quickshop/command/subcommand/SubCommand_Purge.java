package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SubCommand_Purge implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    public SubCommand_Purge(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        plugin.getShopPurger().purge();
        plugin.text().of(sender, "shop-purged-start").send();
    }


}
