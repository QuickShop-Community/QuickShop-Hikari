package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.history.ShopHistory;
import com.ghostchu.quickshop.history.ShopHistoryGUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_History implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_History(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        final Shop shop = getLookingShop(sender);
        if (shop != null) {
            System.out.println("DEBUGGING!!!!");
            new ShopHistoryGUI(plugin,sender,new ShopHistory(plugin,shop)).open();
        } else {
            plugin.text().of(sender, "not-looking-at-shop").send();
        }
    }

}
