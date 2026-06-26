package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.economy.benefit.BenefitOverflowException;
import com.ghostchu.quickshop.api.economy.benefit.BenefitProvider;
import com.ghostchu.quickshop.api.economy.benefit.BenefitsAlreadyException;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.type.benefit.ShopBenefitAddEvent;
import com.ghostchu.quickshop.api.event.settings.type.benefit.ShopBenefitRemoveEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
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

    switch(parser.getArgs().getFirst()) {
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

      if(!plugin.getConfig().getBoolean("shop.allow-offline-benefit", false) && qUser.getBukkitPlayer().isEmpty()) {

        plugin.text().of(sender, "player-offline", player).send();
        return;
      }

      if(!parser.getArgs().get(2).endsWith("%")) {
        // Force player enter '%' to avoid player type something like 0.01 for 1%
        plugin.text().of(sender, "invalid-percentage", parser.getArgs().getFirst()).send();
        return;
      }
      final String percentageStr = CommonUtil.subBeforeLast(parser.getArgs().get(2), "%");
      Util.mainThreadRun(()->{
        try {
          double percent = Double.parseDouble(percentageStr);
          if(Double.isInfinite(percent) || Double.isNaN(percent)) {
            plugin.text().of(sender, "not-a-number", parser.getArgs().get(2)).send();
            return;
          }

          ShopBenefitAddEvent event = ShopBenefitAddEvent.PRE(shop, qUser, 0.0d, percent);
          event.callEvent();

          event = event.clone(Phase.MAIN);
          if(event.callCancellableEvent()) {

            plugin.text().of(sender, "plugin-cancelled", event.getCancelReason()).send();
            return;
          }

          percent = event.updated();

          if(percent <= 0 || percent >= 100) {
            plugin.text().of(sender, "argument-must-between", "percentage", ">0%", "<100%").send();
            return;
          }

          final BenefitProvider benefit = shop.getShopBenefit();


          benefit.add(qUser, BigDecimal.valueOf(percent / 100d));
          shop.setShopBenefit(benefit);

          event = event.clone(Phase.POST);
          event.callEvent();

          plugin.text().of(sender, "benefit-added", qUser.getDisplay()).send();
        } catch(final NumberFormatException ignore) {
          plugin.text().of(sender, "not-a-number", percentageStr).send();
        } catch(final BenefitOverflowException e) {
          plugin.text().of(sender, "benefit-overflow", (e.benefit().doubleValue() * 100) + "%").send();
        } catch(final BenefitsAlreadyException ignore) {
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

              final BenefitProvider benefit = shop.getShopBenefit();

              final BigDecimal percent = benefit.benefits().getOrDefault(qUser, BigDecimal.ZERO);

              ShopBenefitRemoveEvent event = ShopBenefitRemoveEvent.PRE(shop, qUser, percent, BigDecimal.ZERO);
              event.callEvent();

              event = event.clone(Phase.MAIN);
              if(event.callCancellableEvent()) {

                plugin.text().of(sender, "plugin-cancelled", event.getCancelReason()).send();
                return;
              }

              benefit.remove(qUser);
              shop.setShopBenefit(benefit);

              event = event.clone(Phase.POST);
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

    plugin.text().of(sender, "benefit-query", shop.getShopBenefit().benefits().size()).send();
    Util.asyncThreadRun(()->{

      for(final Map.Entry<QUser, BigDecimal> entry : shop.getShopBenefit().benefits().entrySet()) {

        final String v = MsgUtil.decimalFormat(entry.getValue().multiply(BigDecimal.valueOf(100)));
        plugin.text().of(sender, "benefit-query-list", entry.getKey().getDisplay(), v + "%").send();
      }
    });

  }

  @NotNull
  @Override
  public List<String> onTabComplete(
          @NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return List.of("add", "remove", "query");
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
