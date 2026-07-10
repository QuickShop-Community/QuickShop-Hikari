package com.ghostchu.quickshop.addon.limited.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.limited.Main;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.event.CalendarEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SubCommand_Limit implements CommandHandler<Player> {

  private final QuickShop quickshop;

  public SubCommand_Limit(final QuickShop quickshop) {

    this.quickshop = quickshop;
  }

  @Override
  public void onCommand(final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      quickshop.text().of(sender, "command.wrong-args").send();
      return;
    }
    final Shop shop = getLookingShop(sender);
    if(shop == null) {
      quickshop.text().of(sender, "not-looking-at-shop").send();
      return;
    }
    if (!sender.getUniqueId().equals(shop.getOwner().getUniqueId())){
      quickshop.text().of(sender, "not-managed-shop").send();
      return;
    }

    switch(parser.getArgs().getFirst()) {
      case "set" -> {
        try {
          final int limitAmount = Integer.parseInt(parser.getArgs().get(1));
          if(limitAmount > 0) {
            shop.setExtra(new NamespacedKey(Main.instance, "limit"), limitAmount);
            quickshop.text().of(sender, "addon.limited.success-setup").send();
          } else {
            shop.removeExtra(new NamespacedKey(Main.instance, "limit"));
            shop.removeExtra(new NamespacedKey(Main.instance, "data"));
            quickshop.text().of(sender, "addon.limited.success-remove").send();
          }
        } catch(final NumberFormatException e) {
          quickshop.text().of(sender, "not-a-integer", parser.getArgs().get(1)).send();
        }
      }
      case "unset" -> {
        shop.removeExtra(new NamespacedKey(Main.instance, "limit"));
        shop.removeExtra(new NamespacedKey(Main.instance, "data"));
        quickshop.text().of(sender, "addon.limited.success-remove").send();
      }
      case "reset" -> {
        shop.removeExtra(new NamespacedKey(Main.instance, "data"));
        quickshop.text().of(sender, "addon.limited.success-reset").send();
      }
      case "period" -> {
        try {
          final CalendarEvent.CalendarTriggerType type = CalendarEvent.CalendarTriggerType.valueOf(parser.getArgs().get(1).toUpperCase(Locale.ROOT));
          shop.setExtra(new NamespacedKey(Main.instance, "period"), type.name());
          quickshop.text().of(sender, "addon.limited.success-setup").send();
        } catch(final IllegalArgumentException ignored) {
          quickshop.text().of(sender, "command.wrong-args", parser.getArgs().get(1)).send();
        }
      }
    }
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final String[] cmdArg) {

    if(cmdArg.length < 2) {
      return List.of("set", "unset", "reset", "period");
    }
    if(cmdArg.length < 3) {
      switch(cmdArg[0]) {
        case "set" -> {
          return List.of("<max>");
        }
        case "period" -> {
          return Arrays.stream(CalendarEvent.CalendarTriggerType.values())
                  .filter(e->!e.equals(CalendarEvent.CalendarTriggerType.SECOND))
                  .filter(e->!e.equals(CalendarEvent.CalendarTriggerType.NOTHING_CHANGED))
                  .map(Enum::name).toList();
        }
      }
    }
    return Collections.emptyList();
  }
}
