package com.ghostchu.quickshop.menu.browse;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.config.GuiConfig;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.tnemc.item.AbstractItemStack;
import net.tnemc.item.bukkit.BukkitItemStack;
import net.tnemc.menu.core.Page;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.impl.DataAction;
import net.tnemc.menu.core.icon.action.impl.RunnableAction;
import net.tnemc.menu.core.icon.action.impl.SwitchPageAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopBrowseMenu.BROWSE_FILTER;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.BROWSE_SORT;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.BROWSE_STOCK_ONLY;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SELECTED_ITEM_SHOPS;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SHOPS_PAGE;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SHOP_LIST_PAGE;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigDisplay;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigLangEntry;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigLore;

/**
 * ShopListPage - Shows all shops for a specific item type with price comparison and location
 * information
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class ShopListPage {

  private final String menuName;
  private final int menuRows;

  public ShopListPage(final String menuName, final int menuRows) {

    this.menuName = menuName;
    this.menuRows = Math.max(menuRows, 3);
  }

  public void handle(final PageOpenCallback callback) {

    final Optional<MenuViewer> viewerOpt = callback.getPlayer().viewer();
    if(viewerOpt.isEmpty()) return;

    final MenuViewer viewer = viewerOpt.get();
    final Page menuPage = callback.getPage();
    menuPage.setLockEmptySlots(true);

    final Optional<Object> shopsData = viewer.findData(SELECTED_ITEM_SHOPS);
    final UUID id = viewer.uuid();
    final Player player = Bukkit.getPlayer(id);

    if(shopsData.isEmpty() || player == null) return;

    menuPage.getIcons().clear();

    // Load GUI configuration
    final GuiConfig.MenuConfig menuConfig = QuickShop.getInstance().getGuiConfig().getMenuConfig("browse");
    final GuiConfig.IconConfig borderConfig = menuConfig != null? menuConfig.getIcon("border") : null;
    final GuiConfig.IconConfig backConfig = menuConfig != null? menuConfig.getIcon("back") : null;
    final GuiConfig.IconConfig itemInfoConfig = menuConfig != null? menuConfig.getIcon("item-info") : null;
    final GuiConfig.IconConfig sortConfig = menuConfig != null? menuConfig.getIcon("sort") : null;
    final GuiConfig.IconConfig closeConfig = menuConfig != null? menuConfig.getIcon("close") : null;
    final GuiConfig.IconConfig prevPageConfig = menuConfig != null? menuConfig.getIcon("previous-page") : null;
    final GuiConfig.IconConfig nextPageConfig = menuConfig != null? menuConfig.getIcon("next-page") : null;
    final GuiConfig.IconConfig pageInfoConfig = menuConfig != null? menuConfig.getIcon("page-info") : null;

    final int listStartSlot = menuConfig != null? menuConfig.getSection().getInt("list-start-slot", 9) : 9;

    // Get current state
    final BrowseSortMode sortMode = (BrowseSortMode)viewer.dataOrDefault(BROWSE_SORT, BrowseSortMode.PRICE_ASC);
    final BrowseFilterMode filterMode = (BrowseFilterMode)viewer.dataOrDefault(BROWSE_FILTER, BrowseFilterMode.ALL);
    final boolean stockOnly = (Boolean)viewer.dataOrDefault(BROWSE_STOCK_ONLY, false);
    final int page = (Integer)viewer.dataOrDefault(SHOP_LIST_PAGE, 1);

    @SuppressWarnings("unchecked")
    final List<Shop> allShops = (ArrayList<Shop>)shopsData.get();

    // Apply filter and stock filter, then sort
    List<Shop> filteredShops = MarketUtils.filterShops(allShops, filterMode);
    filteredShops = MarketUtils.filterByStock(filteredShops, stockOnly);
    final List<Shop> sortedShops = MarketUtils.sortShops(filteredShops, sortMode);

    // Calculate average price for comparison indicators
    final double avgPrice = sortedShops.isEmpty()? 0 :
                            CommonUtil.avg(sortedShops.stream().map(Shop::getPrice).toList());

    // Calculate pagination (same pattern as MainPage)
    final int offset = 9;
    final int items = (menuRows - 2) * offset;
    final int start = ((page - 1) * offset);
    final int maxPages = (sortedShops.size() / items) + (((sortedShops.size() % items) > 0)? 1 : 0);
    final int prev = (page <= 1)? maxPages : page - 1;
    final int next = (page >= maxPages)? 1 : page + 1;

    // Set up border rows
    final String borderMaterial = borderConfig != null? borderConfig.getMaterial() : "GRAY_STAINED_GLASS_PANE";
    final IconBuilder borderBuilder = new IconBuilder(QuickShop.getInstance().stack().of(borderMaterial, 1));
    final List<Integer> borderRows = borderConfig != null? borderConfig.getRows() : List.of(1, 6);
    for(final int row : borderRows) {
      menuPage.setRow(row, borderBuilder);
    }

    // === Control Row (Row 1) ===
    // Layout: [Item Info] [Sort] [Filter] [Stock] [Back]
    // Uses same slot positions as GroupedItemPage for consistency

    // Item info - shows what item we're viewing (slot 0 - top left)
    final GuiConfig.IconConfig searchConfig = menuConfig != null? menuConfig.getIcon("search") : null;
    final int itemInfoSlot = searchConfig != null? searchConfig.getSlot() : 0;
    if(!allShops.isEmpty()) {
      final GuiConfig.IconConfig searchItemConfig = menuConfig != null? menuConfig.getIcon("search-item") : null;
      final Shop firstShop = allShops.getFirst();
      final Component filterIndicator = QuickShop.getInstance().text().of(id, filterMode.indicatorTranslationKey()).forLocale();
      final AbstractItemStack<ItemStack> infoStack = new BukkitItemStack()
              .of(firstShop.getItem().getType().key().asString(), 1)
              .customName(getConfigDisplay(id, searchItemConfig, "<yellow>{0}</yellow>", Util.getItemStackName(firstShop.getItem())))
              .lore(getConfigLore(id, searchItemConfig, filterIndicator, sortedShops.size(), formatPrice(avgPrice)));

      menuPage.addIcon(new IconBuilder(infoStack).withSlot(itemInfoSlot).build());
    }

    // Sort button (slot 2)
    final String sortMaterial = sortConfig != null? sortConfig.getMaterial() : "HOPPER";
    final int sortSlot = sortConfig != null? sortConfig.getSlot() : 2;
    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(sortMaterial, 1)
                                             .customName(getConfigDisplay(id, sortConfig, "<green>Sort: {0}</green>", QuickShop.getInstance().text().of(id, sortMode.getTranslationKey()).forLocale()))
                                             .lore(getConfigLore(id, sortConfig)))
                             .withSlot(sortSlot)
                             .withActions(
                                     new DataAction(BROWSE_SORT, sortMode.next()),
                                     new DataAction(SHOP_LIST_PAGE, 1),
                                     new SwitchPageAction(menuName, 2)
                                         )
                             .build());

    // Filter button (slot 4)
    final GuiConfig.IconConfig filterConfig = menuConfig != null? menuConfig.getIcon("filter") : null;
    final String filterMaterial = filterConfig != null? filterConfig.getMaterial() : "NAME_TAG";
    final int filterSlot = filterConfig != null? filterConfig.getSlot() : 4;

    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(filterMaterial, 1)
                                             .customName(getConfigDisplay(id, filterConfig, "<aqua>Filter: {0}</aqua>", QuickShop.getInstance().text().of(id, filterMode.getTranslationKey()).forLocale()))
                                             .lore(getConfigLore(id, filterConfig)))
                             .withSlot(filterSlot)
                             .withActions(
                                     new DataAction(BROWSE_FILTER, filterMode.next()),
                                     new DataAction(SHOP_LIST_PAGE, 1),
                                     new SwitchPageAction(menuName, 2)
                                         )
                             .build());

    // Stock filter toggle button (slot 6)
    final GuiConfig.IconConfig stockConfig = menuConfig != null? menuConfig.getIcon("stock-filter") : null;
    final String stockMaterial = stockConfig != null? stockConfig.getMaterial() : "CHEST";
    final int stockSlot = stockConfig != null? stockConfig.getSlot() : 6;
    final String stockStatus = stockOnly? "ON" : "OFF";

    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(stockMaterial, 1)
                                             .customName(getConfigDisplay(id, stockConfig, "<gold>In Stock Only: {0}</gold>", stockStatus))
                                             .lore(getConfigLore(id, stockConfig)))
                             .withSlot(stockSlot)
                             .withActions(
                                     new DataAction(BROWSE_STOCK_ONLY, !stockOnly),
                                     new DataAction(SHOP_LIST_PAGE, 1),
                                     new SwitchPageAction(menuName, 2)
                                         )
                             .build());

    // Back button
    final String backMaterial = backConfig != null? backConfig.getMaterial() : "OAK_DOOR";
    final int backSlot = backConfig != null? backConfig.getSlot() : 8;

    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(backMaterial, 1)
                                             .customName(getConfigDisplay(id, backConfig, "<white>Back to Market</white>")))
                             .withSlot(backSlot)
                             .withActions(
                                     new DataAction(SHOPS_PAGE, 1),
                                     new SwitchPageAction(menuName, 1) // Go back to grouped view
                                         )
                             .build());

    // === Pagination Row (Bottom) ===
    final String prevMaterial = prevPageConfig != null? prevPageConfig.getMaterial() : "ARROW";
    final int prevSlot = prevPageConfig != null? prevPageConfig.getSlot() : 48;
    final String nextMaterial = nextPageConfig != null? nextPageConfig.getMaterial() : "ARROW";
    final int nextSlot = nextPageConfig != null? nextPageConfig.getSlot() : 50;
    final String pageInfoMaterial = pageInfoConfig != null? pageInfoConfig.getMaterial() : "BOOK";
    final int pageInfoSlot = pageInfoConfig != null? pageInfoConfig.getSlot() : 49;

    if(maxPages > 1) {
      menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(prevMaterial, 1)
                                               .customName(getConfigDisplay(id, prevPageConfig, "<white><< Previous Page</white>")))
                               .withSlot(prevSlot)
                               .withActions(new DataAction(SHOP_LIST_PAGE, prev), new SwitchPageAction(menuName, 2))
                               .build());

      menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(nextMaterial, 1)
                                               .customName(getConfigDisplay(id, nextPageConfig, "<white>Next Page >></white>")))
                               .withSlot(nextSlot)
                               .withActions(new DataAction(SHOP_LIST_PAGE, next), new SwitchPageAction(menuName, 2))
                               .build());
    }

    // Page info
    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(pageInfoMaterial, 1)
                                             .customName(getConfigDisplay(id, pageInfoConfig, "<yellow>Page {0}/{1}</yellow>", page, Math.max(1, maxPages))))
                             .withSlot(pageInfoSlot)
                             .build());

    // Check if player has teleport permission
    final boolean canTeleport = QuickShop.getInstance().perm().hasPermission(player, "quickshop.browse.teleport");

    // === Shop Grid ===
    int i = 0;
    for(final Shop shop : sortedShops) {
      if(i < start) {
        i++;
        continue;
      }
      if(i >= (start + items)) break;

      final GuiConfig.IconConfig listConfig = menuConfig != null? menuConfig.getIcon("shop-list") : null;

      // Build shop lore with price indicator and click instruction
      final List<Component> lore = buildShopLore(shop, id, listConfig, avgPrice, canTeleport);

      // Get display name for the item
      final Component itemName = Util.getItemStackName(shop.getItem());

      final AbstractItemStack<ItemStack> stack = new BukkitItemStack()
              .of(shop.getItem().getType().key().asString(), shop.getShopStackingAmount())
              .customName(getConfigDisplay(id, listConfig, "<yellow>{0}</yellow>", itemName))
              .lore(lore);

      // Get teleport location - use shop location + 1 block up
      // Note: We avoid calling shop.getSigns() here as it requires block access
      // which can fail on Folia when the shop is in a different region
      final Location teleportTarget = shop.bukkitLocation().clone().add(0.5, 1, 0.5);

      final IconBuilder iconBuilder = new IconBuilder(stack)
              .withSlot(listStartSlot + (i - start));

      // Only add teleport action if player has permission
      if(canTeleport) {
        // Capture for lambda
        final Location finalTeleportTarget = teleportTarget;
        final Location shopLoc = shop.bukkitLocation().clone().add(0.5, 0.5, 0.5);

        iconBuilder.withActions(new RunnableAction((click)->{
          final Player p = Bukkit.getPlayer(click.player().identifier());
          if(p != null) {
            p.closeInventory();
            // Teleport player to sign location, facing the shop
            final Location teleportLoc = finalTeleportTarget.clone();
            // Calculate direction to look at shop
            final double dx = shopLoc.getX() - teleportLoc.getX();
            final double dz = shopLoc.getZ() - teleportLoc.getZ();

            teleportLoc.setYaw((float)Math.toDegrees(Math.atan2(-dx, dz)));
            teleportLoc.setPitch(30);

            p.teleportAsync(teleportLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
          }
        }));
      }

      menuPage.addIcon(iconBuilder.build());

      i++;
    }
  }

  /**
   * Build the lore for an individual shop. Note: Uses database cache for stock/space to avoid Folia
   * cross-region block access issues.
   */
  private List<Component> buildShopLore(final Shop shop, final UUID playerID, final GuiConfig.IconConfig config, final double avgPrice, final boolean canTeleport) {

    final PriceIndicator indicator = getPriceIndicator(config, shop.getPrice(), avgPrice, shop.isSelling());

    // Location
    final String world = shop.bukkitLocation().getWorld() != null?
                         shop.bukkitLocation().getWorld().getName() : "Unknown";
    final String coords = shop.bukkitLocation().getBlockX() + ", " +
                          shop.bukkitLocation().getBlockY() + ", " +
                          shop.bukkitLocation().getBlockZ();
    final String type = (shop.isSelling())? "selling-label" : "buying-label";

    final QuickShop plugin = QuickShop.getInstance();

    final var mm = QuickShop.getInstance().platform().miniMessage();

    //stock
    final String tradingStringKey = (shop.isStackingShop()? shop.shopType().stackTradingTranslationKey() : shop.shopType().tradingTranslationKey());
    final String finalTradingStringKey = (shop.shopState().overrideShopTypeText())? shop.shopState().translationKey() : tradingStringKey;
    final String noRemainingStringKey = shop.shopType().outOfStockTranslationKey();
    final int shopRemaining = shop.shopType().remainingStock(shop);
    //todo: use cache methods.

    final Component trading = switch(shopRemaining) {
      //Unlimited
      case -1 -> plugin.text().of(playerID, finalTradingStringKey, plugin.text().of(playerID, "signs.unlimited").forLocale()).forLocale();
      //No remaining
      case 0 -> {
        if(shop.shopState().overrideShopTypeText()) {
          yield plugin.text().of(playerID, shop.shopState().translationKey()).forLocale();
        }
        yield plugin.text().of(playerID, noRemainingStringKey).forLocale();
      }
      //Has remaining
      default -> plugin.text().of(playerID, finalTradingStringKey, Component.text(shopRemaining)).forLocale();
    };

    final String color = config.section().getString(indicator.colorPath(shop.isSelling()), "<white>");
    final Component indicatorLang = getConfigLangEntry(playerID, config, indicator.labelPath(shop.isSelling()), "<white>");
    final Component price = mm.deserialize(color + formatPrice(shop.getPrice()) + " ").append(indicatorLang);
    final List<Component> lore = getConfigLore(playerID, config,
                                               shop.getOwner().getDisplay(),
                                               QuickShop.getInstance().text().of(playerID, "gui.browse.shop-list." + type).forLocale(),
                                               price,
                                               trading,
                                               world,
                                               coords);

    // Click instruction (only if player has teleport permission)
    if(canTeleport) {
      lore.add(QuickShop.getInstance().text().of(playerID, "gui.browse.shop-list.teleport").forLocale());
    }

    return lore;
  }

  /**
   * Get a price indicator based on comparison to average
   */
  private PriceIndicator getPriceIndicator(final GuiConfig.IconConfig config, final double price, final double avgPrice, final boolean isSelling) {

    if(avgPrice == 0 || config.section() == null) return PriceIndicator.AVERAGE;

    final double ratio = price / avgPrice;

    if (isSelling) {
      if (ratio < config.section().getDouble(PriceIndicator.GREAT.ratioPath(true)))
        return PriceIndicator.GREAT;

      if (ratio < config.section().getDouble(PriceIndicator.GOOD.ratioPath(true)))
        return PriceIndicator.GOOD;

      if (ratio > config.section().getDouble(PriceIndicator.WORST.ratioPath(true)))
        return PriceIndicator.WORST;

      if (ratio > config.section().getDouble(PriceIndicator.BAD.ratioPath(true)))
        return PriceIndicator.BAD;
    } else {
      if (ratio > config.section().getDouble(PriceIndicator.GREAT.ratioPath(false)))
        return PriceIndicator.GREAT;

      if (ratio > config.section().getDouble(PriceIndicator.GOOD.ratioPath(false)))
        return PriceIndicator.GOOD;

      if (ratio < config.section().getDouble(PriceIndicator.WORST.ratioPath(false)))
        return PriceIndicator.WORST;

      if (ratio < config.section().getDouble(PriceIndicator.BAD.ratioPath(false)))
        return PriceIndicator.BAD;
    }

    return PriceIndicator.AVERAGE;
  }

  /**
   * Format a price value
   */
  private String formatPrice(final double price) {

    return QuickShop.getInstance().getEconomyManager().provider().format(BigDecimal.valueOf(price), null, null);
  }

  /**
   * Get stock count from database cache. This avoids Folia cross-region block access issues by
   * using cached data instead of directly accessing the shop's inventory.
   *
   * @param shop The shop to get stock for
   *
   * @return Stock count, or -1 for unlimited shops
   */
  private int getStockFromCache(final Shop shop) {

    return MarketUtils.getStockFromCache(shop);
  }

  /**
   * Get space count from database cache. This avoids Folia cross-region block access issues by
   * using cached data instead of directly accessing the shop's inventory.
   *
   * @param shop The shop to get space for
   *
   * @return Space count, or -1 for unlimited shops
   */
  private int getSpaceFromCache(final Shop shop) {

    return MarketUtils.getSpaceFromCache(shop);
  }
}
