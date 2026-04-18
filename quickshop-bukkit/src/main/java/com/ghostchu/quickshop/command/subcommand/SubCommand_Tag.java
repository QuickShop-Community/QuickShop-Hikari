package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.tag.TaggingResult;
import com.ghostchu.quickshop.util.pagination.Pagination;
import com.ghostchu.quickshop.util.pagination.PaginationOptions;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import static com.ghostchu.quickshop.api.shop.tag.TagService.TOTAL_INDEX;
import static com.ghostchu.quickshop.shop.tag.QuickShopTagManager.MAX_PER_PAGE;

public class SubCommand_Tag implements CommandHandler<Player> {

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

    switch(sub) {
      case "tags" -> handleShopsTagged(sender, commandLabel, parser);
      case "tagged" -> handleTaggedList(sender, commandLabel, parser);
      case "purge", "removefromall", "untagall" -> handleRemoveTagFromAllShops(sender, parser);
      case "add" -> handleAdd(sender, parser, 1);
      case "remove", "del", "delete" -> handleRemove(sender, parser);
      case "clear" -> handleClear(sender, parser);
      case "clearall" -> handleClearAll(sender, parser); //todo: confirmation maybe for clear and clearall?
      case "list" -> handleTags(sender, commandLabel, parser);
      default -> handleAdd(sender, parser, 0);
    }

    System.out.println("Tag command: " + sub + " Parser Args: " + parser.getArgs().size() + "");

    int i = 0;
    for(final String arg : parser.getArgs()) {
      System.out.println("Arg " + i + ": " + arg);
      i++;
    }
  }

  private void handleAdd(final Player sender, final CommandParser parser, final int tagIndex) {

    if(parser.getArgs().size() <= tagIndex) {
      sendUsage(sender);
      return;
    }

    final Shop shop = findShop(sender, parser, tagIndex + 1);
    if(shop == null) {

      plugin.text().of(sender, "not-looking-at-shop").send();
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

  private void handleRemove(final Player sender, final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      sendUsage(sender);
      return;
    }

    final Shop shop = findShop(sender, parser, 2);
    if(shop == null) {

      plugin.text().of(sender, "not-looking-at-shop").send();
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

  private void handleClear(final Player sender, final CommandParser parser) {

    final Shop shop = findShop(sender, parser, 1);
    if(shop == null) {

      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }

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

  private void handleShopsTagged(final Player sender, @NotNull final String commandLabel, final CommandParser parser) {

    final TreeMap<Long, Integer> count = plugin.tagManager().tagsCount(sender.getUniqueId());
    if(count.isEmpty()) {
      plugin.text().of(sender, "tags.tag.no-tagged-shops").send();
      return;
    }

    final int page = (parser.getArgs().size() >= 2)? Integer.parseInt(parser.getArgs().get(1)) : 1;

    final PaginationOptions<String> options = PaginationOptions
            .builder()
            .setCommand(commandLabel + " tag tags")
            .setCurrentPage(page)
            .setEntries(List.copyOf(count.keySet()))
            .setMaxPerPage(MAX_PER_PAGE)
            .setEntryConsumer((pos, entry)->{

              if(entry.equals(TOTAL_INDEX)) {
                return;
              }

              plugin.text().of(sender, "tags.tag.list-player-shop-entry",
                               pos,
                               entry,
                               count.getOrDefault((Long)entry, -1),
                               commandLabel + " tag list 1 " + entry).send();
            })
            .setHeaderLanguageKey("pagination.header")
            .setFooterLanguageKey("pagination.footer").build();

    final Pagination<String> shops = new Pagination<>(options);

    final Component titleComponent = plugin.text().of(sender, "tags.tag.list-player-shops-title", count.size() - 1, count.get(TOTAL_INDEX)).forLocale();

    shops.printHeader(sender, titleComponent, shops.page(), shops.totalPages());
    shops.printEntries(sender);
    shops.printFooter(sender, options.command() + " " + shops.previousPage(),
                      shops.page(), shops.totalPages(),
                      options.command() + " " + shops.nextPage());
  }

  private void handleTags(final Player sender, @NotNull final String commandLabel, final CommandParser parser) {

    final Shop shop = findShop(sender, parser, 2);
    if(shop == null) {

      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }

    final List<String> tags = List.copyOf(plugin.tagManager().tagsFilteredByShop(sender.getUniqueId(), shop.getShopId()));
    if(tags.isEmpty()) {
      plugin.text().of(sender, "tags.tag.no-tagged-shops-player", shop.getShopId()).send();
      return;
    }

    final int page = (parser.getArgs().size() >= 2)? Integer.parseInt(parser.getArgs().get(1)) : 1;

    final PaginationOptions<String> options = PaginationOptions
            .builder()
            .setCommand(commandLabel + " tag list")
            .setCurrentPage(page)
            .setEntries(tags)
            .setMaxPerPage(MAX_PER_PAGE)
            .setEntryConsumer((pos, entry)->{
              plugin.text().of(sender, "tags.general.list-entry",
                               pos,
                               entry,
                               commandLabel + " " + plugin.tagManager().service().commandFromTag((String)entry, true),
                               commandLabel + " " + plugin.tagManager().service().commandFromTag((String)entry, false) + " " + shop.getShopId()).send();
            })
            .setHeaderLanguageKey("pagination.header")
            .setFooterLanguageKey("pagination.footer").build();

    final Pagination<String> shops = new Pagination<>(options);

    final Component titleComponent = plugin.text().of(sender, "tags.tag.list-player-shop-title", tags.size()).forLocale();

    shops.printHeader(sender, titleComponent, shops.page(), shops.totalPages());
    shops.printEntries(sender);
    shops.printFooter(sender, options.command() + " " + shops.previousPage(),
                      shops.page(), shops.totalPages(),
                      options.command() + " " + shops.nextPage());
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

    final PaginationOptions<Long> options = PaginationOptions
            .builder()
            .setCommand(commandLabel + " tag tagged " + normalized + "")
            .setCurrentPage(page)
            .setEntries(shopIds)
            .setMaxPerPage(MAX_PER_PAGE)
            .setEntryConsumer((pos, entry)->{

              final Shop shop = plugin.getShopManager().getShop((Long)entry);
              if(shop == null) {
                return;
              }

              //TODO: Option to teleport through list? Maybe add a /qs teleport command for this?
              //final boolean canTeleport = plugin.perm().hasPermission(sender, "quickshop.tagged.teleport");
              //final Component tpComponent = plugin.text().of(sender, "tags.tag.list-tag-shop-entry-tp", commandLabel + " tp ").forLocale();

              plugin.text().of(sender, "tags.tag.list-tag-shop-entry", pos, entry, shop.getOwner().getDisplay(), shop.getItem().displayName(), "", commandLabel + " tag remove " + normalized).send();
            })
            .setHeaderLanguageKey("pagination.header")
            .setFooterLanguageKey("pagination.footer").build();

    final Pagination<Long> shops = new Pagination<>(options);


    final Component titleComponent = plugin.text().of(sender, "tags.tag.list-tag-title", shopIds.size()).forLocale();

    shops.printHeader(sender, titleComponent, shops.page(), shops.totalPages());
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
