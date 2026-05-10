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
    final GuiConfig.IconConfig sortConfig = menuConfig != null? menuConfig.getIcon("shop-list-sort") : null;
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

    @SuppressWarnings("unchecked") final List<Shop> allShops = (ArrayList<Shop>)shopsData.get();

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
      final Shop firstShop = allShops.getFirst();
      final String filterIndicator = getFilterIndicator(filterMode);
      final AbstractItemStack<ItemStack> infoStack = new BukkitItemStack()
              .of(firstShop.getItem().getType().key().asString(), 1)
              .display(QuickShop.getInstance().platform().miniMessage().deserialize(
                      "<yellow>" + CommonUtil.prettifyText(firstShop.getItem().getType().name()) + "</yellow>"))
              .lore(List.of(
                      QuickShop.getInstance().platform().miniMessage().deserialize("<gray>Showing: " + filterIndicator + "</gray>"),
                      QuickShop.getInstance().platform().miniMessage().deserialize("<gray>Shops: <white>" + sortedShops.size() + "</white></gray>"),
                      QuickShop.getInstance().platform().miniMessage().deserialize("<gray>Average price: <gold>" + formatPrice(avgPrice) + "</gold></gray>")
                           ));

      menuPage.addIcon(new IconBuilder(infoStack).withSlot(itemInfoSlot).build());
    }

    // Sort button (slot 2)
    final String sortMaterial = sortConfig != null? sortConfig.getMaterial() : "HOPPER";
    final int sortSlot = sortConfig != null? sortConfig.getSlot() : 2;
    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(sortMaterial, 1)
                                             .display(getConfigDisplay(id, sortConfig, "<green>Sort: {0}</green>", getSortDisplayName(sortMode)))
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
                                             .display(getConfigDisplay(id, filterConfig, "<aqua>Filter: {0}</aqua>", getFilterDisplayName(filterMode)))
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
                                             .display(getConfigDisplay(id, stockConfig, "<gold>In Stock Only: {0}</gold>", stockStatus))
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
                                             .display(getConfigDisplay(id, backConfig, "<white>Back to Market</white>")))
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
                                               .display(getConfigDisplay(id, prevPageConfig, "<white><< Previous Page</white>")))
                               .withSlot(prevSlot)
                               .withActions(new DataAction(SHOP_LIST_PAGE, prev), new SwitchPageAction(menuName, 2))
                               .build());

      menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(nextMaterial, 1)
                                               .display(getConfigDisplay(id, nextPageConfig, "<white>Next Page >></white>")))
                               .withSlot(nextSlot)
                               .withActions(new DataAction(SHOP_LIST_PAGE, next), new SwitchPageAction(menuName, 2))
                               .build());
    }

    // Page info
    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(pageInfoMaterial, 1)
                                             .display(getConfigDisplay(id, pageInfoConfig, "<yellow>Page {0}/{1}</yellow>", page, Math.max(1, maxPages))))
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

      // Build shop lore with price indicator and click instruction
      final List<Component> lore = buildShopLore(shop, avgPrice, canTeleport);

      // Get display name for the item
      final String itemName = CommonUtil.prettifyText(shop.getItem().getType().name());

      final AbstractItemStack<ItemStack> stack = new BukkitItemStack()
              .of(shop.getItem().getType().key().asString(), shop.getShopStackingAmount())
              .display(QuickShop.getInstance().platform().miniMessage().deserialize("<yellow>" + itemName + "</yellow>"))
              .lore(lore);

      List<Location> possiblePositions = new ArrayList<>();
      possiblePositions.add(shop.bukkitLocation().clone().add(1, 0, 0));
      possiblePositions.add(shop.bukkitLocation().clone().add(-1, 0, 0));
      possiblePositions.add(shop.bukkitLocation().clone().add(0, 0, 1));
      possiblePositions.add(shop.bukkitLocation().clone().add(0, 0, -1));

      // Check which position has a sign in it
      Location teleportTarget = null;
      for(Location loc : possiblePositions) {
          if(loc.getBlock().getState() instanceof org.bukkit.block.Sign) {
              teleportTarget = loc.add(0.5, 0, 0.5); // Center of the block
              break;
          }
      }


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
  private List<Component> buildShopLore(final Shop shop, final double avgPrice, final boolean canTeleport) {

    final List<Component> lore = new ArrayList<>();
    final var mm = QuickShop.getInstance().platform().miniMessage();

    // Owner
    //TODO: use admin-shop when it's an admin shop
    lore.add(mm.deserialize("<gray>Owner: <white>" + shop.getOwner().getDisplay() + "</white></gray>"));

    // Shop type
    final String typeColor = shop.isSelling()? "<green>" : "<#FFA500>";
    final String typeText = shop.isSelling()? "Selling" : "Buying";
    lore.add(mm.deserialize("<gray>Type: " + typeColor + typeText + "</gray>"));

    // Price with indicator
    final String priceIndicator = getPriceIndicator(shop.getPrice(), avgPrice, shop.isSelling());
    final String priceColor = getPriceColor(priceIndicator);
    lore.add(mm.deserialize("<gray>Price: " + priceColor + formatPrice(shop.getPrice()) + " " + priceIndicator + "</gray>"));

    // Stock/Space - Use database cache to avoid Folia cross-region block access
    // The shop may be in a different region than the player viewing the menu
    if(shop.isSelling()) {
      final int stock = getStockFromCache(shop);
      final String stockText = stock < 0? "Unlimited" : String.valueOf(stock);
      lore.add(mm.deserialize("<gray>Stock: <aqua>" + stockText + "</aqua></gray>"));
    } else {
      final int space = getSpaceFromCache(shop);
      final String spaceText = space < 0? "Unlimited" : String.valueOf(space);
      lore.add(mm.deserialize("<gray>Space: <aqua>" + spaceText + "</aqua></gray>"));
    }

    // Location
    final String world = shop.bukkitLocation().getWorld() != null?
                         shop.bukkitLocation().getWorld().getName() : "Unknown";
    final String coords = shop.bukkitLocation().getBlockX() + ", " +
                          shop.bukkitLocation().getBlockY() + ", " +
                          shop.bukkitLocation().getBlockZ();
    lore.add(mm.deserialize("<gray>Location: <white>" + world + "</white></gray>"));
    lore.add(mm.deserialize("<dark_gray>" + coords + "</dark_gray>"));

    // Click instruction (only if player has teleport permission)
    if(canTeleport) {
      lore.add(Component.empty());
      lore.add(mm.deserialize("<yellow>Click to teleport</yellow>"));
    }

    return lore;
  }

  /**
   * Get a price indicator based on comparison to average
   */
  private String getPriceIndicator(final double price, final double avgPrice, final boolean isSelling) {

    if(avgPrice == 0) return "";

    final double ratio = price / avgPrice;

    if(isSelling) {
      // For selling shops: lower is better for buyers
      if(ratio < 0.85) return "▼▼ Great Deal!";
      if(ratio < 0.95) return "▼ Below Avg";
      if(ratio > 1.15) return "▲▲ Expensive";
      if(ratio > 1.05) return "▲ Above Avg";
    } else {
      // For buying shops: higher is better for sellers
      if(ratio > 1.15) return "▲▲ Great Price!";
      if(ratio > 1.05) return "▲ Above Avg";
      if(ratio < 0.85) return "▼▼ Low Offer";
      if(ratio < 0.95) return "▼ Below Avg";
    }
    return "● Average";
  }

  /**
   * Get color based on price indicator
   */
  private String getPriceColor(final String indicator) {

    if(indicator.contains("Great")) return "<green>";
    if(indicator.contains("Below") || indicator.contains("Low")) return "<yellow>";
    if(indicator.contains("Above")) return "<gold>";
    if(indicator.contains("Expensive")) return "<red>";
    return "<white>";
  }

  /**
   * Get display name for sort mode
   */
  private String getSortDisplayName(final BrowseSortMode mode) {

    return switch(mode) {
      case PRICE_ASC -> "Price ↑";
      case PRICE_DESC -> "Price ↓";
      case STOCK -> "Stock";
      case NAME -> "Name";
    };
  }

  /**
   * Get display name for filter mode
   */
  private String getFilterDisplayName(final BrowseFilterMode mode) {

    return switch(mode) {
      case ALL -> "All";
      case BUYING -> "Buying";
      case SELLING -> "Selling";
    };
  }

  /**
   * Get colored indicator for current filter mode
   */
  private String getFilterIndicator(final BrowseFilterMode mode) {

    return switch(mode) {
      case ALL -> "<white>All Shops</white>";
      case BUYING -> "<#FFA500>Buying Shops</#FFA500>";
      case SELLING -> "<green>Selling Shops</green>";
    };
  }

  /**
   * Format a price value
   */
  private String formatPrice(final double price) {

    return QuickShop.getInstance().getEconomyManager().provider()
            .format(BigDecimal.valueOf(price), null, null);
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
