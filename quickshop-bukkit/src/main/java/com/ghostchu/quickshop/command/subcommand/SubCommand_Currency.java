package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.PriceLimiter;
import com.ghostchu.quickshop.api.shop.PriceLimiterCheckResult;
import com.ghostchu.quickshop.api.shop.PriceLimiterStatus;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SubCommand_Currency implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_Currency(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        final Shop shop = getLookingShop(sender);
        if (shop != null) {
            if (shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_CURRENCY)
                    || plugin.perm().hasPermission(sender, "quickshop.other.currency")) {
                if (parser.getArgs().size() < 1) {
                    shop.setCurrency(null);
                    plugin.text().of(sender, "currency-unset").send();
                    return;
                }
                if (!plugin.getEconomy().supportCurrency()) {
                    plugin.text().of(sender, "currency-not-support").send();
                    return;
                }
                if (!plugin.getEconomy().hasCurrency(Objects.requireNonNull(shop.getLocation().getWorld()), parser.getArgs().get(0))) {
                    plugin.text().of(sender, "currency-not-exists").send();
                    return;
                }

                PriceLimiter limiter = plugin.getShopManager().getPriceLimiter();
                PriceLimiterCheckResult checkResult = limiter.check(sender, shop.getItem(), parser.getArgs().get(0), shop.getPrice());
                if (checkResult.getStatus() != PriceLimiterStatus.PASS) {
                    plugin.text().of(sender, "restricted-prices", Util.getItemStackName(shop.getItem()),
                            Component.text(checkResult.getMin()),
                            Component.text(checkResult.getMax())).send();
                    return;
                }
                shop.setCurrency(parser.getArgs().get(0));
                plugin.text().of(sender, "currency-set", parser.getArgs().get(0)).send();
                return;

            } else {
                plugin.text().of(sender, "not-managed-shop").send();
            }
            return;
        }
        plugin.text().of(sender, "not-looking-at-shop").send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return Collections.emptyList();
    }

}
