package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.economy.Benefit;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.type.benefit.ShopBenefitAddEvent;
import com.ghostchu.quickshop.api.event.settings.type.benefit.ShopBenefitRemoveEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SubCommand_Benefit implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Benefit(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      plugin.text().of(sender, "command-incorrect", "/quickshop benefit <add/remove/query> <player> <percentage>").send();
      return;
    }
    final Shop shop = getLookingShop(sender);
    if(shop == null) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }
    // Check permission
    if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_BENEFIT)
       && !plugin.perm().hasPermission(sender, "quickshop.other.benefit")) {
      plugin.text().of(sender, "not-managed-shop").send();
      return;
    }

    switch(parser.getArgs().get(0)) {
      case "add" -> addBenefit(sender, shop, parser);
      case "remove" -> removeBenefit(sender, shop, parser);
      case "query" -> queryBenefit(sender, shop, parser);
      default ->
              plugin.text().of(sender, "command-incorrect", "/quickshop benefit <add/remove> <player> <percentage>").send();
    }

  }

  private void addBenefit(final Player sender, final Shop shop, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() < 3) {
      plugin.text().of(sender, "command-incorrect", "/quickshop benefit <add/remove> <player> <percentage>").send();
      return;
    }
    final String player = parser.getArgs().get(1);

    QUserImpl.createAsync(plugin.getPlayerFinder(), player).thenAccept(qUser->{
      if(qUser == null) {
        plugin.text().of(sender, "unknown-player", player).send();
        return;
      }

      if(!PackageUtil.parsePackageProperly("allowOffline").asBoolean()) {
        if(qUser.getBukkitPlayer().isEmpty()) {
          plugin.text().of(sender, "player-offline", player).send();
          return;
        }
      }
      if(!parser.getArgs().get(2).endsWith("%")) {
        // Force player enter '%' to avoid player type something like 0.01 for 1%
        plugin.text().of(sender, "invalid-percentage", parser.getArgs().get(0)).send();
        return;
      }
      final String percentageStr = StringUtils.substringBeforeLast(parser.getArgs().get(2), "%");
      Util.mainThreadRun(()->{
        try {
          double percent = Double.parseDouble(percentageStr);
          if(Double.isInfinite(percent) || Double.isNaN(percent)) {
            plugin.text().of(sender, "not-a-number", parser.getArgs().get(2)).send();
            return;
          }

          ShopBenefitAddEvent event = (ShopBenefitAddEvent)ShopBenefitAddEvent.PRE(shop, 0.0d, percent);
          event.callEvent();

          event = (ShopBenefitAddEvent)event.clone(Phase.MAIN);
          if(event.callCancellableEvent()) {

            plugin.logger().info("Plugin cancelled ShopBenefitAddEvent");
            plugin.text().of(sender, "internal-error").send();
            return;
          }

          percent = event.updated();

          if(percent <= 0 || percent >= 100) {
            plugin.text().of(sender, "argument-must-between", "percentage", ">0%", "<100%").send();
            return;
          }

          final Benefit benefit = shop.getShopBenefit();


          benefit.addBenefit(qUser, percent / 100d);
          shop.setShopBenefit(benefit);

          event = (ShopBenefitAddEvent)event.clone(Phase.POST);
          event.callEvent();

          plugin.text().of(sender, "benefit-added", qUser.getDisplay()).send();
        } catch(final NumberFormatException e) {
          plugin.text().of(sender, "not-a-number", percentageStr).send();
        } catch(final Benefit.BenefitOverflowException e) {
          plugin.text().of(sender, "benefit-overflow", (e.getOverflow() * 100) + "%").send();
        } catch(final Benefit.BenefitExistsException e) {
          plugin.text().of(sender, "benefit-exists").send();
        }
      });
    }).exceptionally(e->{
      plugin.logger().warn("Failed to get uuid of player " + player, e);
      plugin.text().of(sender, "internal-error").send();
      return null;
    });

  }

  private void removeBenefit(final Player sender, final Shop shop, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      plugin.text().of(sender, "command-incorrect", "/quickshop benefit <add/remove/query> <player> <percentage>").send();
      return;
    }
    final String player = parser.getArgs().get(1);

    QUserImpl.createAsync(plugin.getPlayerFinder(), player).thenAccept((qUser)->{
              if(qUser == null) {
                plugin.text().of(sender, "unknown-player", player).send();
                return;
              }

              final Benefit benefit = shop.getShopBenefit();

              final Double percent = benefit.getRegistry().getOrDefault(qUser, 0.0d);

              ShopBenefitRemoveEvent event = (ShopBenefitRemoveEvent)ShopBenefitRemoveEvent.PRE(shop, percent, 0.0d);
              event.callEvent();

              event = (ShopBenefitRemoveEvent)event.clone(Phase.MAIN);
              if(event.callCancellableEvent()) {

                plugin.logger().info("Plugin cancelled ShopBenefitRemoveEvent");
                plugin.text().of(sender, "internal-error").send();
                return;
              }

              benefit.removeBenefit(qUser);
              shop.setShopBenefit(benefit);

              event = (ShopBenefitRemoveEvent)event.clone(Phase.POST);
              event.callEvent();

              plugin.text().of(sender, "benefit-removed", qUser.getDisplay()).send();
            })
            .exceptionally(e->{
              plugin.logger().warn("Failed to get uuid of player " + player, e);
              plugin.text().of(sender, "internal-error").send();
              return null;
            });

  }

  private void queryBenefit(final Player sender, final Shop shop, @NotNull final CommandParser parser) {

    plugin.text().of(sender, "benefit-query", shop.getShopBenefit().getRegistry().size()).send();
    Util.asyncThreadRun(()->{

      for(final Map.Entry<QUser, Double> entry : shop.getShopBenefit().getRegistry().entrySet()) {

        final String v = MsgUtil.decimalFormat(entry.getValue() * 100);
        plugin.text().of(sender, "benefit-query-list", entry.getKey().getDisplay(), v + "%").send();
      }
    });

  }

  @NotNull
  @Override
  public List<String> onTabComplete(
          @NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return List.of("add", "remove");
    }
    if(parser.getArgs().size() == 2) {
      return null;
    }
    if(parser.getArgs().size() == 3) {
      return Collections.singletonList(plugin.text().of(sender, "tabcomplete.percentage").legacy());
    }
    return Collections.emptyList();
  }

}
