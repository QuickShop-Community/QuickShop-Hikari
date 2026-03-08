package com.ghostchu.quickshop.addon.tags.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.tags.Main;
import com.ghostchu.quickshop.addon.tags.TagManager;
import com.ghostchu.quickshop.addon.tags.TagService;
import com.ghostchu.quickshop.addon.tags.tag.TaggingResult;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.ghostchu.quickshop.addon.tags.TagService.TOTAL_INDEX;

public class SubCommand_Tag implements CommandHandler<Player> {

  private final Main main;
  private final QuickShop plugin;
  private final TagManager tagManager;

  public SubCommand_Tag(final Main main, final QuickShop plugin, final TagManager tagManager) {

    this.main = main;
    this.plugin = plugin;
    this.tagManager = tagManager;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel,
                        @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      sendUsage(sender);
      return;
    }

    final String sub = parser.getArgs().getFirst().toLowerCase(Locale.ROOT);

    // Global commands that do not require looking at a shop.
    switch(sub) {
      case "tags" -> {
        handleTags(sender);
        return;
      }
      case "tagged" -> {
        handleTaggedList(sender, parser);
        return;
      }
      case "purge", "removefromall", "untagall" -> {
        handleRemoveTagFromAllShops(sender, parser);
        return;
      }
      default -> {
      }
    }

    final Shop shop = getLookingShop(sender);
    if(shop == null) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }

    if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_BENEFIT)
       && !plugin.perm().hasPermission(sender, "quickshop.other.benefit")) {
      plugin.text().of(sender, "not-managed-shop").send();
      return;
    }

    switch(sub) {
      case "add" -> handleAdd(sender, shop, parser, 2);
      case "remove", "del", "delete" -> handleRemove(sender, shop, parser);
      case "clear" -> handleClear(sender, shop);
      case "clearall" ->
              handleClearAll(sender, parser); //todo: confirmation maybe for clear and clearall?
      case "list" -> handleTags(sender, shop);
      default -> handleAdd(sender, shop, parser, 1);
    }
  }

  private void handleAdd(final Player sender, final Shop shop,
                         final CommandParser parser, final int tagIndex) {

    if(parser.getArgs().size() <= tagIndex) {
      sendUsage(sender);
      return;
    }

    final String normalized = TagService.normalizeTag(parser.getArgs().get(tagIndex), false);
    if(normalized == null) {
      plugin.text().of(sender, "addon.tags.general.invalid").send();
      return;
    }

    final TaggingResult result = tagManager.addTag(shop.getShopId(), sender.getUniqueId(), normalized);
    switch(result) {
      case SUCCESS ->
              plugin.text().of(sender, "addon.tags.tag.added", TagService.displayTag(sender, normalized)).send();
      case ALREADY_EXISTS ->
              plugin.text().of(sender, "addon.tags.tag.duplicate", TagService.displayTag(sender, normalized)).send();
      case INVALID_TAG -> plugin.text().of(sender, "addon.tags.general.invalid").send();
      case DATABASE_ERROR -> plugin.text().of(sender, "addon.tags.general.database-error").send();
      default ->
              plugin.text().of(sender, "addon.tags.tag.failed-add", TagService.displayTag(sender, normalized)).send();
    }
  }

  private void handleRemove(final Player sender, final Shop shop, final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      sendUsage(sender);
      return;
    }

    final String normalized = TagService.normalizeTag(parser.getArgs().get(1), false);
    if(normalized == null) {
      plugin.text().of(sender, "addon.tags.general.invalid").send();
      return;
    }

    final TaggingResult result = tagManager.removeTag(shop.getShopId(), sender.getUniqueId(), normalized);
    switch(result) {
      case SUCCESS ->
              plugin.text().of(sender, "addon.tags.tag.removed", TagService.displayTag(sender, normalized)).send();
      case NOT_FOUND ->
              plugin.text().of(sender, "addon.tags.tag.does-not-exist", TagService.displayTag(sender, normalized)).send();
      case DATABASE_ERROR -> plugin.text().of(sender, "addon.tags.general.database-error").send();
      default ->
              plugin.text().of(sender, "addon.tags.tag.failed-remove", TagService.displayTag(sender, normalized)).send();
    }
  }

  private void handleClear(final Player sender, final Shop shop) {

    final boolean removed = tagManager.removeAllShopTagsBy(shop.getShopId(), sender.getUniqueId());
    if(removed) {
      plugin.text().of(sender, "addon.tags.tag.cleared", shop.getShopId()).send();
    } else {
      plugin.text().of(sender, "addon.tags.tag.no-tags", shop.getShopId()).send();
    }
  }

  private void handleClearAll(final Player sender, final CommandParser parser) {

    if(!plugin.perm().hasPermission(sender, "quickshop.tag.admin.clearall")) {
      plugin.text().of(sender, "no-permission").send();
      return;
    }

    final boolean removed = tagManager.removeAllTags();
    if(removed) {
      plugin.text().of(sender, "addon.tags.tag.cleared-all").send();
    } else {
      plugin.text().of(sender, "addon.tags.tag.no-tags-all").send();
    }
  }

  private void handleTags(final Player sender) {

    final TreeMap<Long, Integer> count = tagManager.tagsCount(sender.getUniqueId());
    if(count.isEmpty()) {
      plugin.text().of(sender, "addon.tags.tag.no-tagged-shops").send();
      return;
    }

    plugin.text().of(sender, "addon.tags.tag.list-player-shops-title", count.get(TOTAL_INDEX), count.size()).send();
    for(final Map.Entry<Long, Integer> entry : count.entrySet()) {

      plugin.text().of(sender, "addon.tags.tag.list-player-shop-entry", entry.getKey(), entry.getValue()).send();
    }
  }

  private void handleTags(final Player sender, final Shop shop) {

    final Set<String> tags = Collections.unmodifiableSet(tagManager.tagsFilteredByShop(sender.getUniqueId(), shop.getShopId()));
    if(tags.isEmpty()) {
      plugin.text().of(sender, "addon.tags.tag.no-tagged-shops-player", shop.getShopId()).send();
      return;
    }

    plugin.text().of(sender, "addon.tags.tag.list-player-shop-title", tags.size(), shop.getShopId()).send();
    for(final String tag : tags) {
      plugin.text().of(sender, "addon.tags.general.list-entry", tag).send();
    }
  }

  private void handleTaggedList(final Player sender, final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      sendUsage(sender);
      return;
    }

    final String normalized = TagService.normalizeTag(parser.getArgs().get(1), false);
    if(normalized == null) {
      plugin.text().of(sender, "addon.tags.general.invalid").send();
      return;
    }

    final List<Long> shopIds = tagManager.shopsFilteredByTag(sender.getUniqueId(), normalized);
    if(shopIds.isEmpty()) {
      plugin.text().of(sender, "addon.tags.tag.no-tagged-shops-tag", normalized).send();
      return;
    }

    plugin.text().of(sender, "addon.tags.tag.list-tag-title", shopIds.size()).send();
    for(final Long shopId : shopIds) {
      plugin.text().of(sender, "addon.tags.general.list-entry", shopId).send();
    }
  }

  private void handleRemoveTagFromAllShops(final Player sender, final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      sendUsage(sender);
      return;
    }

    final String normalized = TagService.normalizeTag(parser.getArgs().get(1), false);
    if(normalized == null) {
      plugin.text().of(sender, "addon.tags.general.invalid").send();
      return;
    }

    final boolean cleared = tagManager.removeTag(sender.getUniqueId(), normalized);
    if(!cleared) {
      plugin.text().of(sender, "addon.tags.general.database-error", normalized).send();
      return;
    }

    plugin.text().of(sender, "addon.tags.tag.clearing-tag", normalized).send();
  }

  @NotNull
  @Override
  public List<String> onTabComplete(@NotNull final Player sender, @NotNull final String commandLabel,
                                    @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return List.of("add", "remove", "clear", "clearall", "list", "tags", "tagged", "purge");
    }
    return Collections.emptyList();
  }

  private void sendUsage(final Player sender) {

    plugin.text().of(sender, "command-incorrect",
                     "/quickshop tag <add/remove/clear/clearall/list/tags/tagged/purge> [tag]")
            .send();
  }
}
