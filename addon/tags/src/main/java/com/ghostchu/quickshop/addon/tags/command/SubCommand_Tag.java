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

import static com.ghostchu.quickshop.addon.tags.Util.SYS_AVOID;
import static com.ghostchu.quickshop.addon.tags.Util.SYS_FAV;
import static com.ghostchu.quickshop.addon.tags.Util.SYS_WATCH;
import static com.ghostchu.quickshop.addon.tags.Util.handleAdd;
import static com.ghostchu.quickshop.addon.tags.Util.handleClear;
import static com.ghostchu.quickshop.addon.tags.Util.handleClearAll;
import static com.ghostchu.quickshop.addon.tags.Util.handleRemove;
import static com.ghostchu.quickshop.addon.tags.Util.handleRemoveTagFromAllShops;
import static com.ghostchu.quickshop.addon.tags.Util.handleTaggedList;
import static com.ghostchu.quickshop.addon.tags.Util.handleTags;
import static com.ghostchu.quickshop.addon.tags.Util.normalizeTag;

public class SubCommand_Tag implements CommandHandler<Player> {

  private final QuickShop plugin;
  //our system tags

  public SubCommand_Tag(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      sendUsage(sender);
      return;
    }

    final String sub = parser.getArgs().getFirst();

    //These are our global tag-related commands. They don't require looking at a shop.
    switch (sub) {
      // /qs tag tags
      case "tags" -> {
        handleTags(sender);
        return;
      }
      // /qs tag tagged <tag>
      case "tagged" -> {
        handleTaggedList(sender, parser);
        return;
      }
      // /qs tag purge <tag>
      case "purge", "removefromall", "untagall" -> {
        handleRemoveTagFromAllShops(sender, parser);
        return;
      }
      default -> {}
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
      case "add" -> handleAdd(sender, shop, parser, 2);
      case "remove", "del", "delete" -> handleRemove(sender, shop, parser);
      case "clear" -> handleClear(sender, shop);

      // Optional: admin "clear all tags from this shop for everyone"
      case "clearall" -> handleClearAll(sender, shop);

      case "list" -> handleTags(sender, shop);

      default -> handleAdd(sender, shop, parser, 1);
    }
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

  private void sendUsage(final Player sender) {
    plugin.text().of(sender, "command-incorrect",
                     "/quickshop tag <[tag]/remove/list/clear/tagged/tags> [player]")
          .send();
  }
}
