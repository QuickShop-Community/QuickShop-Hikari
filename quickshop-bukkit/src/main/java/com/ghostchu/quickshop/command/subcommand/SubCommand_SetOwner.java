package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.event.ShopOwnershipTransferEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.ghostchu.quickshop.util.Util.getPlayerList;

public class SubCommand_SetOwner implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_SetOwner(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "command.no-owner-given").send();
            return;
        }
        final Shop shop = getLookingShop(sender);

        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }

        if (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.OWNERSHIP_TRANSFER)
                && !plugin.perm().hasPermission(sender, "quickshop.other.setowner")) {
            plugin.text().of(sender, "no-permission").send();
            return;
        }

        Profile newShopOwner = plugin.getPlayerFinder().find(cmdArg[0]);
        if (newShopOwner == null) {
            plugin.text().of(sender, "unknown-player").send();
            return;
        }
        ShopOwnershipTransferEvent event = new ShopOwnershipTransferEvent(shop, shop.getOwner(), newShopOwner.getUniqueId());
        if (event.callCancellableEvent()) {
            return;
        }
        shop.setOwner(newShopOwner.getUniqueId());
        plugin.text().of(sender, "command.new-owner", newShopOwner.getName()).send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length <= 1 ? getPlayerList() : Collections.emptyList();
    }

}
