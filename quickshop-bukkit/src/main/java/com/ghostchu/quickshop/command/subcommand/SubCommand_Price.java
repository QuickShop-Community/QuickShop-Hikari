package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.ShopUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class SubCommand_Price implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Price(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      plugin.text().of(sender, "no-price-given").send();
      return;
    }

    BigDecimal price = null;
    try {
      price = Util.parse(parser.getArgs().getFirst());
    } catch(final Exception ignore) {
    }

    if(price == null) {
      // No number input
      Log.debug("Price sub command had issue with price parameter.");
      plugin.text().of(sender, "not-a-number", parser.getArgs().getFirst()).send();
      return;
    }

    final double priceDouble = price.doubleValue();
    // No number input
    if(Double.isInfinite(priceDouble) || Double.isNaN(priceDouble)) {
      plugin.text().of(sender, "not-a-number", parser.getArgs().getFirst()).send();
      return;
    }
    final Shop shop = getLookingShop(sender);
    if(shop == null) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }

    ShopUtil.setPrice(plugin, QUserImpl.createFullFilled(sender), priceDouble, shop);
  }

  @NotNull
  @Override
  public List<String> onTabComplete(
          @NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    return parser.getArgs().size() == 1? Collections.singletonList(plugin.text().of(sender, "tabcomplete.price").plain()) : Collections.emptyList();
  }

}
