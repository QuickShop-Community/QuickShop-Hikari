package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_Freeze implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_Freeze(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        final Shop shop = getLookingShop(sender);
        if (shop != null) {
            if (shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_SHOPTYPE)
                    || plugin.perm().hasPermission(sender, "quickshop.other.buy")) {

                if(shop.getShopType().equals(ShopType.FROZEN)) {

                    shop.setShopType(ShopType.BUYING);
                    plugin.text().of(sender, "shop-nolonger-freezed", Util.getItemStackName(shop.getItem())).send();
                    plugin.text().of(sender, "command.now-buying", Util.getItemStackName(shop.getItem())).send();
                } else {

                    shop.setShopType(ShopType.FROZEN);
                    plugin.text().of(sender, "shop-now-freezed", Util.getItemStackName(shop.getItem())).send();
                }
                shop.setSignText(plugin.text().findRelativeLanguages(sender));
            } else {
                plugin.text().of(sender, "not-managed-shop").send();
            }
            return;
        }
        plugin.text().of(sender, "not-looking-at-shop").send();
    }
}