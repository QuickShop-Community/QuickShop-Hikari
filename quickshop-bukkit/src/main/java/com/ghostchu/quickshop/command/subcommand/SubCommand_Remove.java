package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_Remove implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_Remove(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        final Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        if (shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.DELETE)
                || plugin.perm().hasPermission(sender, "quickshop.other.destroy")) {
            shop.delete();
            plugin.logEvent(new ShopRemoveLog(sender.getUniqueId(), "/qs remove command", shop.saveToInfoStorage()));
        } else {
            plugin.text().of(sender, "no-permission").send();
        }
    }

}
