package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_SilentRemove extends SubCommand_SilentBase {

    public SubCommand_SilentRemove(QuickShop plugin) {
        super(plugin);
    }

    @Override
    protected void doSilentCommand(@NotNull Player sender, @NotNull Shop shop, @NotNull CommandParser parser) {
        if (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.DELETE)
                && !plugin.perm().hasPermission(sender, "quickshop.other.destroy")) {
            plugin.text().of(sender, "no-permission").send();
            return;
        }

        plugin.logEvent(new ShopRemoveLog(sender.getUniqueId(), "/qs silentremove command", shop.saveToInfoStorage()));
        shop.delete();
    }
}
