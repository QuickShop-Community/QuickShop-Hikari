package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.shop.history.ShopHistory;
import com.ghostchu.quickshop.shop.history.ShopHistoryGUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_SilentHistory extends SubCommand_SilentBase {

    public SubCommand_SilentHistory(QuickShop plugin) {
        super(plugin);
    }

    @Override
    protected void doSilentCommand(Player sender, @NotNull Shop shop, @NotNull CommandParser parser) {
        if (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.VIEW_PURCHASE_LOGS)
                && !plugin.perm().hasPermission(sender, "quickshop.other.history")) {
            plugin.text().of(sender, "not-managed-shop").send();
            return;
        }
        new ShopHistoryGUI(plugin, sender, new ShopHistory(plugin, shop)).open();
    }
}
