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
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SubCommand_Tag implements CommandHandler<Player> {

  private final QuickShop plugin;
  //our system tags
  private static final String SYS_FAV = "@fav";
  private static final String SYS_WATCH = "@watch";
  private static final String SYS_AVOID = "@avoid";

  private static final int MAX_TAG_LENGTH = 32;
  //we only want letters, underscores and dashes
  private static final Pattern VALID_TAG_PATTERN = Pattern.compile("^[a-z_-]+$");

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
      // /qs tag favs
      case "favs" -> {
        handleTaggedList(sender, new ArrayDeque<>(List.of(SYS_FAV)));
        return;
      }
      // /qs tag watched
      case "watched" -> {
        handleTaggedList(sender, new ArrayDeque<>(List.of(SYS_WATCH)));
        return;
      }
      // /qs tag avoided
      case "avoided" -> {
        handleTaggedList(sender, new ArrayDeque<>(List.of(SYS_AVOID)));
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

      // System tag shortcuts (looked-at shop)
      case "fav" -> handleToggleSystem(sender, shop, SYS_FAV);
      case "unfav" -> handleUnsetSystem(sender, shop, SYS_FAV);

      case "watch" -> handleToggleSystem(sender, shop, SYS_WATCH);
      case "unwatch" -> handleUnsetSystem(sender, shop, SYS_WATCH);

      case "avoid" -> handleToggleSystem(sender, shop, SYS_AVOID);
      case "unavoid" -> handleUnsetSystem(sender, shop, SYS_AVOID);

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
                     "/quickshop tag <[tag]/remove/list/clear/tagged/tags/fav/unfav/favs/watch/unwatch/watched/avoid/unavoid/avoided>")
          .send();
  }

  //our shop-specific methods
  private void handleAdd(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser, final int tagPosition) {
    if(parser.getArgs().size() < tagPosition) {
      sendUsage(sender);
      return;
    }

    final String tag = normalizeTag(parser.getArgs().get(tagPosition - 1), false);
    if(tag == null) {
      plugin.text().of(sender, "tag-invalid").send();
      return;
    }
    Util.regionThread(sender.getLocation(), () -> {
      plugin.getDatabaseHelper().tagShop(sender.getUniqueId(), shop.getShopId(), tag);
      plugin.text().of(sender, "tag-added", tag).send();
    });
  }

  private void handleRemove(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser) {
    if(parser.getArgs().size() < 2) {
      sendUsage(sender);
      return;
    }
  }

  private void handleClear(final Player sender, @NotNull final Shop shop) {

  }

  private void handleToggleSystem(final Player sender, @NotNull final Shop shop, final String tag) {

  }

  private void handleUnsetSystem(final Player sender, @NotNull final Shop shop, final String tag) {

  }

  private void handleClearAll(final Player sender, @NotNull final Shop shop) {

    //TODO: User parameter
    if(!sender.hasPermission("quickshop.tag.clearall")) {
      plugin.text().of(sender, "no-permission").send();
      return;
    }


  }

  //our global methods
  private void handleTaggedList(final Player sender, @NotNull final CommandParser parser) {
  }

  private void handleTaggedList(final Player sender, @NotNull final ArrayDeque<String> tags) {
  }

  private void handleTags(final Player sender) {

  }

  private void handleTags(final Player sender, @NotNull final Shop shop) {

  }

  private void handleRemoveTagFromAllShops(final Player sender, @NotNull final CommandParser parser) {

  }

  private String displayTag(final Player sender, final String stored) {
    if(stored == null) {
      return "";
    }

    return switch (stored) {
      case SYS_FAV -> "Favorite";
      case SYS_WATCH -> "Watch";
      case SYS_AVOID -> "Avoid";
      default -> "#" + stored;
    };
  }

  private String displayTagOrHash(final String stored) {
    if(stored == null) {
      return "";
    }

    if(stored.startsWith("@")) {
      return stored;
    }
    return "#" + stored;
  }

  @Nullable
  private String normalizeTag(@Nullable String input, final boolean allowSystem) {

    if (input == null) {
      return null;
    }

    if (input.startsWith("#")) {
      input = input.substring(1);
    }

    input = input.trim().toLowerCase(Locale.ROOT);

    if (input.isEmpty()) {
      return null;
    }

    if (input.length() > MAX_TAG_LENGTH) {
      return null;
    }

    if (!allowSystem && input.startsWith("@")) {
      return null;
    }

    if (!VALID_TAG_PATTERN.matcher(input).matches()) {
      return null;
    }

    return input;
  }
}
