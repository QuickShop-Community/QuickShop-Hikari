package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.tag.TagService;
import com.ghostchu.quickshop.api.shop.tag.TaggingResult;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class SubCommand_Watch implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Watch(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender,
                        @NotNull final String commandLabel,
                        @NotNull final CommandParser parser) {

    final String tag = plugin.tagManager().service().normalizeTag(TagService.SYS_WATCH, true);
    if(tag == null) {

      //should never happen, but we'll catch just in case.
      plugin.text().of(sender, "tags.general.invalid").send();
      return;
    }

    if(parser.getArgs().isEmpty()) {

      final Shop shop = getLookingShop(sender);
      if(shop == null) {
        plugin.text().of(sender, "not-looking-at-shop").send();
        return;
      }

      final TaggingResult result = plugin.tagManager().toggleTag(shop.getShopId(), sender.getUniqueId(), tag);

      switch(result) {
        case TaggingResult.SUCCESS -> {
          if(plugin.tagManager().hasTag(shop.getShopId(), sender.getUniqueId(), tag)) {
            plugin.text().of(sender, "tags.watch.added").send();
          } else {
            plugin.text().of(sender, "tags.watch.removed").send();
          }
        }
        case TaggingResult.DATABASE_ERROR ->
                plugin.text().of(sender, "tags.general.database-error").send();
        default -> plugin.text().of(sender, "tags.watch.unable").send();
      }
      return;
    }

    final String sub = parser.getArgs().getFirst();
    switch(sub.toLowerCase(Locale.ROOT)) {
      case "list" -> {

        plugin.tagManager().listShopsByFilter(sender, new ArrayList<>(List.of(tag)),
                                                       "tags.watch.list-title",
                                                       "tags.watch.none");
      }
      default -> sendUsage(sender);
    }
  }

  @NotNull
  @Override
  public List<String> onTabComplete(@NotNull final Player sender,
                                    @NotNull final String commandLabel,
                                    @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return List.of("list");
    }
    return Collections.emptyList();
  }

  private void sendUsage(final Player sender) {

    plugin.text().of(sender, "command-incorrect", "/quickshop watch [list]").send();
  }
}