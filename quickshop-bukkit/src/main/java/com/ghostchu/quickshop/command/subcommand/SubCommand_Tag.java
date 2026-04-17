package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.tag.TaggingResult;
import com.ghostchu.quickshop.util.pagination.Pagination;
import com.ghostchu.quickshop.util.pagination.PaginationOptions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.ghostchu.quickshop.api.shop.tag.TagService.TOTAL_INDEX;

public class SubCommand_Tag implements CommandHandler<Player> {

  private final int maxPerPage = 5;

  private final QuickShop plugin;

  public SubCommand_Tag(final QuickShop plugin) {

    this.plugin = plugin;
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
        handleTaggedList(sender, commandLabel, parser);
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

    /*if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_TAG)
       && !plugin.perm().hasPermission(sender, "quickshop.other.tag")) {
      plugin.text().of(sender, "not-managed-shop").send();
      return;
    }*/

    System.out.println("Tag command: " + sub + " Parser Args: " + parser.getArgs().size() + "");

    switch(sub) {
      case "add" -> handleAdd(sender, shop, parser, 1);
      case "remove", "del", "delete" -> handleRemove(sender, shop, parser);
      case "clear" -> handleClear(sender, shop);
      case "clearall" ->
              handleClearAll(sender, parser); //todo: confirmation maybe for clear and clearall?
      case "list" -> handleTags(sender, commandLabel, parser, shop);
      default -> handleAdd(sender, shop, parser, 0);
    }
  }

  private void handleAdd(final Player sender, final Shop shop,
                         final CommandParser parser, final int tagIndex) {

    if(parser.getArgs().size() <= tagIndex) {
      sendUsage(sender);
      return;
    }

    final String normalized = plugin.tagManager().service().normalizeTag(parser.getArgs().get(tagIndex), false);
    if(normalized == null) {
      plugin.text().of(sender, "tags.general.invalid").send();
      return;
    }

    final TaggingResult result = plugin.tagManager().addTag(shop.getShopId(), sender.getUniqueId(), normalized);
    switch(result) {
      case SUCCESS ->
              plugin.text().of(sender, "tags.tag.added", plugin.tagManager().service().displayTag(sender, normalized)).send();
      case ALREADY_EXISTS ->
              plugin.text().of(sender, "tags.tag.duplicate", plugin.tagManager().service().displayTag(sender, normalized)).send();
      case INVALID_TAG -> plugin.text().of(sender, "tags.general.invalid").send();
      case DATABASE_ERROR -> plugin.text().of(sender, "tags.general.database-error").send();
      default ->
              plugin.text().of(sender, "tags.tag.failed-add", plugin.tagManager().service().displayTag(sender, normalized)).send();
    }
  }

  private void handleRemove(final Player sender, final Shop shop, final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      sendUsage(sender);
      return;
    }

    final String normalized = plugin.tagManager().service().normalizeTag(parser.getArgs().get(1), false);
    if(normalized == null) {
      plugin.text().of(sender, "tags.general.invalid").send();
      return;
    }

    final TaggingResult result = plugin.tagManager().removeTag(shop.getShopId(), sender.getUniqueId(), normalized);
    switch(result) {
      case SUCCESS ->
              plugin.text().of(sender, "tags.tag.removed", plugin.tagManager().service().displayTag(sender, normalized)).send();
      case NOT_FOUND ->
              plugin.text().of(sender, "tags.tag.does-not-exist", plugin.tagManager().service().displayTag(sender, normalized)).send();
      case DATABASE_ERROR -> plugin.text().of(sender, "tags.general.database-error").send();
      default ->
              plugin.text().of(sender, "tags.tag.failed-remove", plugin.tagManager().service().displayTag(sender, normalized)).send();
    }
  }

  private void handleClear(final Player sender, final Shop shop) {

    final boolean removed = plugin.tagManager().removeAllShopTagsBy(shop.getShopId(), sender.getUniqueId());
    if(removed) {
      plugin.text().of(sender, "tags.tag.cleared", shop.getShopId()).send();
    } else {
      plugin.text().of(sender, "tags.tag.no-tags", shop.getShopId()).send();
    }
  }

  private void handleClearAll(final Player sender, final CommandParser parser) {

    if(!plugin.perm().hasPermission(sender, "quickshop.tag.admin.clearall")) {
      plugin.text().of(sender, "no-permission").send();
      return;
    }

    final boolean removed = plugin.tagManager().removeAllTags();
    if(removed) {
      plugin.text().of(sender, "tags.tag.cleared-all").send();
    } else {
      plugin.text().of(sender, "tags.tag.no-tags-all").send();
    }
  }

  private void handleTags(final Player sender) {

    final TreeMap<Long, Integer> count = plugin.tagManager().tagsCount(sender.getUniqueId());
    if(count.isEmpty()) {
      plugin.text().of(sender, "tags.tag.no-tagged-shops").send();
      return;
    }

    plugin.text().of(sender, "tags.tag.list-player-shops-title", count.get(TOTAL_INDEX), count.size()).send();
    for(final Map.Entry<Long, Integer> entry : count.entrySet()) {

      plugin.text().of(sender, "tags.tag.list-player-shop-entry", entry.getKey(), entry.getValue()).send();
    }
  }

  private void handleTags(final Player sender, @NotNull final String commandLabel,
                          final CommandParser parser, final Shop shop) {

    final List<String> tags = List.copyOf(plugin.tagManager().tagsFilteredByShop(sender.getUniqueId(), shop.getShopId()));
    if(tags.isEmpty()) {
      plugin.text().of(sender, "tags.tag.no-tagged-shops-player", shop.getShopId()).send();
      return;
    }

    final int page = (parser.getArgs().size() >= 3)? Integer.parseInt(parser.getArgs().get(2)) : 1;

    final PaginationOptions<String> options = PaginationOptions
            .builder()
            .setCommand(commandLabel + " tag list")
            .setCurrentPage(page)
            .setEntries(tags)
            .setMaxPerPage(maxPerPage)
            .setEntryConsumer((entry)->{
              plugin.text().of(sender, "tags.general.list-entry", entry, commandLabel + " tag tagged " + entry, commandLabel + " tag remove " + entry).send();
            })
            .setHeaderLanguageKey("pagination.header")
            .setFooterLanguageKey("pagination.footer").build();

    final Pagination<String> shops = new Pagination<>(options);

    shops.printHeader(sender, "tags.tag.list-player-shop-entry", shops.page(), shops.totalPages());
    shops.printEntries(sender);
    shops.printFooter(sender, options.command() + " " + shops.previousPage(),
                      shops.page(), shops.totalPages(),
                      options.command() + " " + shops.nextPage());

    /*plugin.text().of(sender, "tags.tag.list-player-shop-title", tags.size(), shop.getShopId()).send();
    for(final String tag : tags) {
      plugin.text().of(sender, "tags.general.list-entry", tag).send();
    }*/
  }

  private void handleTaggedList(final Player sender, @NotNull final String commandLabel, final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      sendUsage(sender);
      return;
    }

    final int page = (parser.getArgs().size() >= 3)? Integer.parseInt(parser.getArgs().get(2)) : 1;

    final String normalized = plugin.tagManager().service().normalizeTag(parser.getArgs().get(1), false);
    if(normalized == null) {
      plugin.text().of(sender, "tags.general.invalid").send();
      return;
    }

    final List<Long> shopIds = plugin.tagManager().shopsFilteredByTag(sender.getUniqueId(), normalized);
    if(shopIds.isEmpty()) {
      plugin.text().of(sender, "tags.tag.no-tagged-shops-tag", normalized).send();
      return;
    }

    /*plugin.text().of(sender, "tags.tag.list-tag-title", shopIds.size()).send();
    for(final Long shopId : shopIds) {
      plugin.text().of(sender, "tags.general.list-entry", shopId).send();
    }*/

    final PaginationOptions<Long> options = PaginationOptions
            .builder()
            .setCommand(commandLabel + " tag tagged " + normalized + "")
            .setCurrentPage(page)
            .setEntries(shopIds)
            .setMaxPerPage(maxPerPage)
            .setEntryConsumer((entry)->{
              plugin.text().of(sender, "tags.general.list-entry", entry).send();
            })
            .setHeaderLanguageKey("pagination.header")
            .setFooterLanguageKey("pagination.footer").build();

    final Pagination<Long> shops = new Pagination<>(options);

    //shops.printHeader(sender, "Placeholder header", shops.page(), shops.totalPages());
    plugin.text().of(sender, "pagination.header", "Placeholder header", shops.page(), shops.totalPages()).send();
    shops.printEntries(sender);
    shops.printFooter(sender, options.command() + " " + shops.previousPage(),
                      shops.page(), shops.totalPages(),
                      options.command() + " " + shops.nextPage());
  }

  private void handleRemoveTagFromAllShops(final Player sender, final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      sendUsage(sender);
      return;
    }

    final String normalized = plugin.tagManager().service().normalizeTag(parser.getArgs().get(1), false);
    if(normalized == null) {
      plugin.text().of(sender, "tags.general.invalid").send();
      return;
    }

    final boolean cleared = plugin.tagManager().removeTag(sender.getUniqueId(), normalized);
    if(!cleared) {
      plugin.text().of(sender, "tags.general.database-error", normalized).send();
      return;
    }

    plugin.text().of(sender, "tags.tag.clearing-tag", normalized).send();
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
