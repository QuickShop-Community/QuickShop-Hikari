package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.type.ShopCurrencyEvent;
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

  public SubCommand_Currency(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final Shop shop = getLookingShop(sender);
    if(shop != null) {
      if(shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_CURRENCY)
         || plugin.perm().hasPermission(sender, "quickshop.other.currency")) {
        if(parser.getArgs().isEmpty()) {

          ShopCurrencyEvent event = (ShopCurrencyEvent)ShopCurrencyEvent.PRE(shop, shop.getCurrency(), null);
          event.callEvent();

          event = (ShopCurrencyEvent)event.clone(Phase.MAIN);
          if(event.callCancellableEvent()) {

            plugin.text().of(sender, "plugin-cancelled", event.getCancelReason());
            return;
          }

          shop.setCurrency(event.updated());
          if(event.updated() != null) {


            plugin.text().of(sender, "currency-set", event.updated()).send();
          } else {

            plugin.text().of(sender, "currency-unset").send();
          }

          event = (ShopCurrencyEvent)event.clone(Phase.POST);
          event.callEvent();
          return;
        }
        if(!plugin.getEconomy().supportCurrency()) {
          plugin.text().of(sender, "currency-not-support").send();
          return;
        }
        if(!plugin.getEconomy().hasCurrency(Objects.requireNonNull(shop.getLocation().getWorld()), parser.getArgs().get(0))) {
          plugin.text().of(sender, "currency-not-exists").send();
          return;
        }

        final PriceLimiter limiter = plugin.getShopManager().getPriceLimiter();


        ShopCurrencyEvent event = (ShopCurrencyEvent)ShopCurrencyEvent.PRE(shop, shop.getCurrency(), parser.getArgs().get(0));
        event.callEvent();

        event = (ShopCurrencyEvent)event.clone(Phase.MAIN);
        if(event.callCancellableEvent()) {

          plugin.text().of(sender, "plugin-cancelled", event.getCancelReason());
          return;
        }

        final PriceLimiterCheckResult checkResult = limiter.check(sender, shop.getItem(), event.updated(), shop.getPrice());
        if(checkResult.getStatus() != PriceLimiterStatus.PASS) {
          plugin.text().of(sender, "restricted-prices", Util.getItemStackName(shop.getItem()),
                           Component.text(checkResult.getMin()),
                           Component.text(checkResult.getMax())).send();
          return;
        }

        shop.setCurrency(event.updated());
        plugin.text().of(sender, "currency-set", event.updated()).send();

        event = (ShopCurrencyEvent)event.clone(Phase.POST);
        event.callEvent();
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
          @NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    return Collections.emptyList();
  }

}
