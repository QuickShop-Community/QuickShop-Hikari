package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.SimpleShopManager;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SubCommand_SilentUnlimited extends SubCommand_SilentBase {

    public SubCommand_SilentUnlimited(QuickShop plugin) {
        super(plugin);
    }

    @Override
    protected void doSilentCommand(Player sender, @NotNull Shop shop, @NotNull CommandParser parser) {
        shop.setUnlimited(!shop.isUnlimited());
        shop.setSignText(plugin.text().findRelativeLanguages(sender));
        MsgUtil.sendControlPanelInfo(sender, shop);

        if (shop.isUnlimited()) {
            plugin.text().of(sender, "command.toggle-unlimited.unlimited").send();
            if (plugin.getConfig().getBoolean("unlimited-shop-owner-change")) {
                UUID uuid = ((SimpleShopManager) plugin.getShopManager()).getCacheUnlimitedShopAccount();
                Util.asyncThreadRun(() -> {
                    String name = plugin.getPlayerFinder().uuid2Name(uuid);
                    if (name == null) {
                        Log.debug("Failed to migrate shop to unlimited shop owner, uniqueid invalid: " + uuid + ".");
                        return;
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
