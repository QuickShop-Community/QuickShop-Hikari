package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.shop.ContainerShop;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class SubCommand_SilentPreview extends SubCommand_SilentBase {

    public SubCommand_SilentPreview(QuickShop plugin) {
        super(plugin);
    }

    @Override
    protected void doSilentCommand(Player sender, @NotNull Shop shop, @NotNull CommandParser parser) {
        if (!(shop instanceof ContainerShop)) {
            // This should never happen
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        if (shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.PREVIEW_SHOP)
                || plugin.perm().hasPermission(sender, "quickshop.other.preview")) {
            shop.openPreview(sender);
        }
    }

}
