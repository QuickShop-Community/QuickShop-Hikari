package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.SimpleShopManager;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SubCommand_Unlimited implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_Unlimited(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        final Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        shop.setUnlimited(!shop.isUnlimited());
        shop.setSignText(plugin.text().findRelativeLanguages(sender));
        if (shop.isUnlimited()) {
            plugin.text().of(sender, "command.toggle-unlimited.unlimited").send();
            if (plugin.getConfig().getBoolean("unlimited-shop-owner-change")) {
                UUID uuid = ((SimpleShopManager) plugin.getShopManager()).getCacheUnlimitedShopAccount();
                Util.asyncThreadRun(() -> {
                    String name = plugin.getPlayerFinder().uuid2Name(uuid);
                    if (name == null) {
                        Log.debug("Failed to migrate shop to unlimited shop owner, uniqueid invalid: " + uuid + ".");
                    }
                    Util.mainThreadRun(() -> {
                        plugin.getShopManager().migrateOwnerToUnlimitedShopOwner(shop);
                        plugin.text().of(sender, "unlimited-shop-owner-changed", name).send();
                    });
                });


            }
            return;
        }
        plugin.text().of(sender, "command.toggle-unlimited.limited").send();
        if (plugin.getConfig().getBoolean("unlimited-shop-owner-change")) {
            plugin.text().of(sender, "unlimited-shop-owner-keeped").send();
        }
    }

}
