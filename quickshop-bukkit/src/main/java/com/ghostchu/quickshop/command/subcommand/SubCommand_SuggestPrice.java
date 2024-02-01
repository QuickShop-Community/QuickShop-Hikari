package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class SubCommand_SuggestPrice implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_SuggestPrice(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        final Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        plugin.text().of(sender, "suggest-wait").send();
        Util.asyncThreadRun(() -> {
            List<Double> matched = plugin.getShopManager().getAllShops().stream()
                    .filter(s -> s.getShopId() != shop.getShopId())
                    .filter(s -> s.getShopType() == shop.getShopType())
                    .filter(s -> Objects.equals(s.getCurrency(), shop.getCurrency()))
                    .filter(s -> plugin.getItemMatcher().matches(shop.getItem(), s.getItem()))
                    .map(Shop::getPrice)
                    .toList();
            if (matched.size() < 3) {
                plugin.text().of(sender, "cannot-suggest-price", matched.size()).send();
                return;
            }
            double min = CommonUtil.min(matched);
            double max = CommonUtil.max(matched);
            double avg = CommonUtil.avg(matched);
            double med = CommonUtil.med(matched);
            plugin.text().of(sender, "price-suggest", matched.size(), format(max, shop), format(min, shop), format(avg, shop), format(med, shop), format(med, shop)).send();
        });
    }

    private String format(double d, Shop shop) {
        return plugin.getShopManager().format(d, shop);
    }
}
