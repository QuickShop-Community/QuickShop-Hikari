package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.shop.history.ShopHistory;
import com.ghostchu.quickshop.shop.history.ShopHistoryGUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_History implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_History(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        if (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.VIEW_PURCHASE_LOGS) && !plugin.perm().hasPermission(sender, "quickshop.other.history")) {
            plugin.text().of(sender, "no-permission");
            return;
        }
        new ShopHistoryGUI(plugin, sender, new ShopHistory(plugin, shop)).open();
    }

}
