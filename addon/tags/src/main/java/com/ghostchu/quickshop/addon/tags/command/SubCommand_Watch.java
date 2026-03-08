package com.ghostchu.quickshop.addon.tags.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.tags.Main;
import com.ghostchu.quickshop.addon.tags.TagService;
import com.ghostchu.quickshop.addon.tags.tag.TaggingResult;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class SubCommand_Watch implements CommandHandler<Player> {

  private final Main main;
  private final QuickShop plugin;

  public SubCommand_Watch(final Main main, final QuickShop plugin) {

    this.main = main;
    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender,
                        @NotNull final String commandLabel,
                        @NotNull final CommandParser parser) {

    final String tag = TagService.normalizeTag(TagService.SYS_WATCH, true);
    if(tag == null) {

      //should never happen, but we'll catch just in case.
      plugin.text().of(sender, "addon.tags.general.invalid").send();
      return;
    }

    if(parser.getArgs().isEmpty()) {

      final Shop shop = getLookingShop(sender);
      if(shop == null) {
        plugin.text().of(sender, "not-looking-at-shop").send();
        return;
      }

      final TaggingResult result = main.tagManager().toggleTag(shop.getShopId(), sender.getUniqueId(), tag);

      switch(result) {
        case SUCCESS -> {
          if(main.tagManager().hasTag(shop.getShopId(), sender.getUniqueId(), tag)) {
            plugin.text().of(sender, "addon.tags.watch.added").send();
          } else {
            plugin.text().of(sender, "addon.tags.watch.removed").send();
          }
        }
        case DATABASE_ERROR -> plugin.text().of(sender, "addon.tags.general.database-error").send();
        default -> plugin.text().of(sender, "addon.tags.watch.unable").send();
      }
      return;
    }

    final String sub = parser.getArgs().getFirst();
    switch(sub.toLowerCase(Locale.ROOT)) {
      case "list" -> {

        Main.instance().tagManager().listShopsByFilter(sender, new ArrayList<>(List.of(tag)),
                                                       "addon.tags.watch.list-title",
                                                       "addon.tags.watch.none");
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