package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SubCommand_Refill implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Refill(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      plugin.text().of(sender, "command.no-amount-given").send();
      return;
    }
    final int add;
    final Shop shop = getLookingShop(sender);
    if(shop == null) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }
    if(StringUtils.isNumeric(parser.getArgs().get(0))) {
      add = Integer.parseInt(parser.getArgs().get(0));
    } else {
      if(parser.getArgs().get(0).equals(plugin.getConfig().getString("shop.word-for-trade-all-items"))) {
        add = shop.getRemainingSpace();
      } else {
        plugin.text().of(sender, "not-a-number", parser.getArgs().get(0)).send();
        return;
      }
    }
    shop.add(shop.getItem(), add);
    shop.setSignText(plugin.text().findRelativeLanguages(sender));
    plugin.text().of(sender, "refill-success").send();
  }

  @NotNull
  @Override
  public List<String> onTabComplete(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    return parser.getArgs().size() == 1? Collections.singletonList(plugin.text().of(sender, "tabcomplete.amount").plain()) : Collections.emptyList();
  }

}
