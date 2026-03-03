package com.ghostchu.quickshop.addon.tags.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.ghostchu.quickshop.addon.tags.Util.SYS_WATCH;
import static com.ghostchu.quickshop.addon.tags.Util.handleTaggedList;

public class SubCommand_Watch implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Watch(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      sendUsage(sender);
      return;
    }

    if(parser.getArgs().size() == 0) {

      final Shop shop = getLookingShop(sender);
      if(shop == null) {
        plugin.text().of(sender, "not-looking-at-shop").send();
        return;
      }

      //TODO: Toggle watch for the shop
      return;
    }

    final String sub = parser.getArgs().getFirst();
    switch(sub.toLowerCase(Locale.ROOT)) {

      case "list": handleTaggedList(sender, new ArrayDeque<>(List.of(SYS_WATCH)));
    }
  }

  @NotNull
  @Override
  public List<String> onTabComplete(
          @NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return List.of("list");
    }
    return Collections.emptyList();
  }

  private void sendUsage(final Player sender) {
    plugin.text().of(sender, "command-incorrect",
                     "/quickshop watch [list]")
            .send();
  }
}