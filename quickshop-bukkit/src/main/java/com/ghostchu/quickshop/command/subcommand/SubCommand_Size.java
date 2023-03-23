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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SubCommand_Size implements CommandHandler<Player> {
    private final QuickShop plugin;

    public SubCommand_Size(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() < 1) {
            plugin.text().of(sender, "command.bulk-size-not-set").send();
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(parser.getArgs().get(0));
        } catch (NumberFormatException e) {
            plugin.text().of(sender, "not-a-integer", parser.getArgs().get(0)).send();
            return;
        }
        final Shop shop = getLookingShop(sender);
        if (shop != null) {
            if (shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_STACK_AMOUNT)
                    || plugin.perm().hasPermission(sender, "quickshop.other.amount")) {
                if (amount <= 0) {
                    plugin.text().of(sender, "command.invalid-bulk-amount", amount).send();
                    return;
                }
                if (amount > Util.getItemMaxStackSize(shop.getItem().getType()) && !plugin.getConfig().getBoolean("shop.disable-max-size-check-for-size-command", false)) {
                    plugin.text().of(sender, "command.invalid-bulk-amount", amount).send();
                    return;
                }
                ItemStack pendingItemStack = shop.getItem().clone();
                pendingItemStack.setAmount(amount);
                PriceLimiter limiter = plugin.getShopManager().getPriceLimiter();
                PriceLimiterCheckResult checkResult = limiter.check(sender, pendingItemStack, shop.getCurrency(), shop.getPrice());
                if (checkResult.getStatus() != PriceLimiterStatus.PASS) {
                    plugin.text().of(sender, "restricted-prices", Util.getItemStackName(shop.getItem()),
                            Component.text(checkResult.getMin()),
                            Component.text(checkResult.getMax())).send();
                    return;
                }
                shop.setItem(pendingItemStack);
                plugin.text().of(sender, "command.bulk-size-now", shop.getItem().getAmount(), Util.getItemStackName(shop.getItem())).send();
            } else {
                plugin.text().of(sender, "not-managed-shop").send();
            }
        } else {
            plugin.text().of(sender, "not-looking-at-shop").send();
        }


    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return parser.getArgs().size() == 1 ? Collections.singletonList(LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "tabcomplete.amount").forLocale())) : Collections.emptyList();
    }
}
