package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class SubCommand_Buy implements CommandHandler<Player> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        final Shop shop = getLookingShop(sender);
        if (shop != null) {
            if (!plugin.perm().hasPermission(sender, "quickshop.create.buy")) {
                plugin.text().of("no-permission").send();
                return;
            }
            if (shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_SHOPTYPE) || plugin.perm().hasPermission(sender, "quickshop.create.admin")) {
                shop.setShopType(ShopType.BUYING);
                shop.setSignText(plugin.text().findRelativeLanguages(sender));
                plugin.text().of(sender, "command.now-buying", MsgUtil.getTranslateText(shop.getItem())).send();
            } else {
                plugin.text().of(sender, "not-managed-shop").send();
            }
            return;
        }
        plugin.text().of(sender, "not-looking-at-shop").send();
    }


}
