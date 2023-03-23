package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.economy.Benefit;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.Profile;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SubCommand_Benefit implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_Benefit(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() < 1) {
            plugin.text().of(sender, "command-incorrect", "/qs benefit <add/remove/query> <player> <percentage>").send();
            return;
        }
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        // Check permission
        if (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_BENEFIT)
                && !plugin.perm().hasPermission(sender, "quickshop.other.benefit")) {
            plugin.text().of(sender, "not-managed-shop").send();
            return;
        }

        switch (parser.getArgs().get(0)) {
            case "add" -> addBenefit(sender, shop, parser);
            case "remove" -> removeBenefit(sender, shop, parser);
            case "query" -> queryBenefit(sender, shop, parser);
            default ->
                    plugin.text().of(sender, "command-incorrect", "/qs benefit <add/remove> <player> <percentage>").send();
        }

    }

    private void addBenefit(Player sender, Shop shop, @NotNull CommandParser parser) {
        if (parser.getArgs().size() < 3) {
            plugin.text().of(sender, "command-incorrect", "/qs benefit <add/remove> <player> <percentage>").send();
            return;
        }
        String player = parser.getArgs().get(1);
        UUID uuid = plugin.getPlayerFinder().name2Uuid(player);
        if (uuid == null) {
            plugin.text().of(sender, "unknown-player", player).send();
            return;
        }
        if (!PackageUtil.parsePackageProperly("allowOffline").asBoolean()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                plugin.text().of(sender, "player-offline", player).send();
                return;
            }
        }
        if (!parser.getArgs().get(2).endsWith("%")) {
            // Force player enter '%' to avoid player type something like 0.01 for 1%
            plugin.text().of(sender, "invalid-percentage", parser.getArgs().get(0)).send();
            return;
        }
        String percentageStr = StringUtils.substringBeforeLast(parser.getArgs().get(2), "%");
        try {
            double percent = Double.parseDouble(percentageStr);
            if (Double.isInfinite(percent) || Double.isNaN(percent)) {
                plugin.text().of(sender, "not-a-number", parser.getArgs().get(2)).send();
                return;
            }
            if (percent <= 0 || percent >= 100) {
                plugin.text().of(sender, "argument-must-between", "percentage", ">0%", "<100%").send();
                return;
            }
            Benefit benefit = shop.getShopBenefit();
            benefit.addBenefit(uuid, percent / 100d);
            shop.setShopBenefit(benefit);
            plugin.text().of(sender, "benefit-added", MsgUtil.formatPlayerProfile(new Profile(uuid, player), sender)).send();
        } catch (NumberFormatException e) {
            plugin.text().of(sender, "not-a-number", percentageStr).send();
        } catch (Benefit.BenefitOverflowException e) {
            plugin.text().of(sender, "benefit-overflow", (e.getOverflow() * 100) + "%").send();
        } catch (Benefit.BenefitExistsException e) {
            plugin.text().of(sender, "benefit-exists").send();
        }
    }

    private void removeBenefit(Player sender, Shop shop, @NotNull CommandParser parser) {
        if (parser.getArgs().size() < 2) {
            plugin.text().of(sender, "command-incorrect", "/qs benefit <add/remove/query> <player> <percentage>").send();
            return;
        }
        String player = parser.getArgs().get(1);
        UUID uuid = plugin.getPlayerFinder().name2Uuid(player);
        if (uuid == null) {
            plugin.text().of(sender, "unknown-player", player).send();
            return;
        }

        Benefit benefit = shop.getShopBenefit();
        benefit.removeBenefit(uuid);
        shop.setShopBenefit(benefit);
        plugin.text().of(sender, "benefit-removed", MsgUtil.formatPlayerProfile(new Profile(uuid, player), sender)).send();

    }

    private void queryBenefit(Player sender, Shop shop, @NotNull CommandParser parser) {
        plugin.text().of(sender, "benefit-query", shop.getShopBenefit().getRegistry().size()).send();
        Util.asyncThreadRun(() -> {
            for (Map.Entry<UUID, Double> entry : shop.getShopBenefit().getRegistry().entrySet()) {
                String v = MsgUtil.decimalFormat(entry.getValue() * 100);
                plugin.text().of(sender, "benefit-query-list", plugin.getPlayerFinder().uuid2Name(entry.getKey()), entry.getKey(), v + "%").send();
            }
        });

    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() == 1) {
            return List.of("add", "remove");
        }
        if (parser.getArgs().size() == 2) {
            return null;
        }
        if (parser.getArgs().size() == 3) {
            return Collections.singletonList(LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "tabcomplete.percentage").forLocale()));
        }
        return Collections.emptyList();
    }

}
