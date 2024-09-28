package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    if(shop == null) {

      final ItemStack stack = sender.getInventory().getItemInMainHand();
      if(stack == null || stack.getType().equals(Material.AIR)) {

        plugin.text().of(sender, "not-looking-at-shop").send();
        return;
      }

      plugin.text().of(sender, "suggest-wait").send();
      Util.asyncThreadRun(()->{
        Shop shop1 = null;

        for(Shop shopTest : plugin.getShopManager().getAllShops()) {
          if(plugin.getItemMatcher().matches(stack, shopTest.getItem())) {
            shop1 = shopTest;
            break;
          }
        }

        if(shop1 == null) {
          plugin.text().of(sender, "cannot-suggest-price", 0).send();
          return;
        }

        final List<Double> matchedBuy = plugin.getShopManager().getAllShops().stream()
                .filter(s->plugin.getItemMatcher().matches(stack, s.getItem()))
                .map(Shop::getPrice)
                .toList();

        final List<Double> matchedSell = plugin.getShopManager().getAllShops().stream()
                .filter(s->plugin.getItemMatcher().matches(stack, s.getItem()))
                .map(Shop::getPrice)
                .toList();

        final int totalSize = (matchedBuy.size() + matchedSell.size());

        if(totalSize < 3) {
          plugin.text().of(sender, "cannot-suggest-price", totalSize).send();
          return;
        }

        double min = CommonUtil.min(matchedBuy);
        double max = CommonUtil.max(matchedBuy);
        double avg = CommonUtil.avg(matchedBuy);
        double med = CommonUtil.med(matchedBuy);

        if(matchedBuy.size() >= 3) {
          final Component suggest = plugin.text().of(sender, "price-suggest", matchedBuy.size(), format(max, shop1), format(min, shop1), format(avg, shop1), format(med, shop1), format(med, shop1)).forLocale();
          plugin.text().of(sender, "price-suggest-multi", plugin.text().of(sender, "shop-type.buying").forLocale(), suggest).send();
        }

        if(matchedSell.size() >= 3) {
          min = CommonUtil.min(matchedSell);
          max = CommonUtil.max(matchedSell);
          avg = CommonUtil.avg(matchedSell);
          med = CommonUtil.med(matchedSell);
          final Component suggest = plugin.text().of(sender, "price-suggest", matchedSell.size(), format(max, shop1), format(min, shop1), format(avg, shop1), format(med, shop1), format(med, shop1)).forLocale();
          plugin.text().of(sender, "price-suggest-multi", plugin.text().of(sender, "shop-type.selling").forLocale(), suggest).send();
        }
      });
      return;
    }
    plugin.text().of(sender, "suggest-wait").send();
    Util.asyncThreadRun(()->{
      List<Double> matched = plugin.getShopManager().getAllShops().stream()
              .filter(s->s.getShopId() != shop.getShopId())
              .filter(s->s.getShopType() == shop.getShopType())
              .filter(s->Objects.equals(s.getCurrency(), shop.getCurrency()))
              .filter(s->plugin.getItemMatcher().matches(shop.getItem(), s.getItem()))
              .map(Shop::getPrice)
              .toList();
      if(matched.size() < 3) {
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
