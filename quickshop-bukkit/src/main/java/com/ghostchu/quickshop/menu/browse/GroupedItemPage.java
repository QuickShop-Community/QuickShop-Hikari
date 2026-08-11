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
import com.ghostchu.quickshop.menu.shared.ClearSearchAction;
import com.ghostchu.quickshop.menu.shared.GuiChatAction;
import net.kyori.adventure.text.Component;
import net.tnemc.item.AbstractItemStack;
import net.tnemc.item.bukkit.BukkitItemStack;
import net.tnemc.menu.core.Page;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.compatibility.MenuPlayer;
import net.tnemc.menu.core.icon.action.ActionType;
import net.tnemc.menu.core.icon.action.impl.DataAction;
import net.tnemc.menu.core.icon.action.impl.RunnableAction;
import net.tnemc.menu.core.icon.action.impl.SwitchPageAction;
import net.tnemc.menu.core.manager.MenuManager;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopBrowseMenu.BROWSE_FILTER;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.BROWSE_SEARCH;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.BROWSE_SORT;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.BROWSE_STOCK_ONLY;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SELECTED_ITEM_SHOPS;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SHOPS_DATA;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SHOPS_PAGE;
import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SHOP_LIST_PAGE;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigDisplay;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigLore;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.guiMessage;

/**
 * GroupedItemPage - Market overview page showing items grouped by type with price statistics and
 * the ability to drill down into specific items
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class GroupedItemPage {

  private final String menuName;
  private final int menuRows;

  public GroupedItemPage(final String menuName, final int menuRows) {

    this.menuName = menuName;
    this.menuRows = Math.max(menuRows, 3); // Minimum 3 rows
  }

  public void handle(final PageOpenCallback callback) {

    final Optional<MenuViewer> viewerOpt = callback.getPlayer().viewer();
    if(viewerOpt.isEmpty()) return;

    final MenuViewer viewer = viewerOpt.get();
    final Page menuPage = callback.getPage();

    final Optional<Object> shopsData = viewer.findData(SHOPS_DATA);
    final UUID id = viewer.uuid();
    final Player player = Bukkit.getPlayer(id);

    if(shopsData.isEmpty() || player == null) return;

    menuPage.getIcons().clear();
    menuPage.setLockEmptySlots(true);

    // Load GUI configuration
    final GuiConfig.MenuConfig menuConfig = QuickShop.getInstance().getGuiConfig().getMenuConfig("browse");
    final GuiConfig.IconConfig borderConfig = (menuConfig != null)? menuConfig.getIcon("border") : null;
    final GuiConfig.IconConfig searchConfig = (menuConfig != null)? menuConfig.getIcon("search") : null;
    final GuiConfig.IconConfig sortConfig = (menuConfig != null)? menuConfig.getIcon("sort") : null;
    final GuiConfig.IconConfig groupedConfig = (menuConfig != null)? menuConfig.getIcon("grouped-item") : null;
    final GuiConfig.IconConfig filterConfig = (menuConfig != null)? menuConfig.getIcon("filter") : null;
    final GuiConfig.IconConfig stockConfig = (menuConfig != null)? menuConfig.getIcon("stock-filter") : null;
    final GuiConfig.IconConfig prevPageConfig = (menuConfig != null)? menuConfig.getIcon("previous-page") : null;
    final GuiConfig.IconConfig nextPageConfig = (menuConfig != null)? menuConfig.getIcon("next-page") : null;
    final GuiConfig.IconConfig pageInfoConfig = (menuConfig != null)? menuConfig.getIcon("page-info") : null;
    final GuiConfig.IconConfig closeConfig = (menuConfig != null)? menuConfig.getIcon("close") : null;

    final int listStartSlot = (menuConfig != null)? menuConfig.getSection().getInt("list-start-slot", 9) : 9;

    // Get current state
    final BrowseSortMode sortMode = (BrowseSortMode)viewer.dataOrDefault(BROWSE_SORT, BrowseSortMode.PRICE_ASC);
    final BrowseFilterMode filterMode = (BrowseFilterMode)viewer.dataOrDefault(BROWSE_FILTER, BrowseFilterMode.ALL);
    final String searchQuery = (String)viewer.dataOrDefault(BROWSE_SEARCH, "");
    final boolean stockOnly = (Boolean)viewer.dataOrDefault(BROWSE_STOCK_ONLY, false);
    final int page = (Integer)viewer.dataOrDefault(SHOPS_PAGE, 1);

    @SuppressWarnings("unchecked")
    final List<Shop> allShops = (ArrayList<Shop>)shopsData.get();

    // Process shops into groups with current filters/sort/search
    final List<MarketItemGroup> groups = MarketUtils.processGroups(allShops, filterMode, sortMode, searchQuery, stockOnly);

    // Calculate pagination (same pattern as MainPage)
    final int offset = 9;
    final int items = (menuRows - 2) * offset;
    final int start = ((page - 1) * offset);
    final int maxPages = (groups.size() / items) + (((groups.size() % items) > 0)? 1 : 0);
    final int prev = (page <= 1)? maxPages : page - 1;
    final int next = (page >= maxPages)? 1 : page + 1;

    // Set up border rows
    final String borderMaterial = (borderConfig != null)? borderConfig.getMaterial() : "GRAY_STAINED_GLASS_PANE";
    final IconBuilder borderBuilder = new IconBuilder(QuickShop.getInstance().stack().of(borderMaterial, 1));
    final List<Integer> borderRows = (borderConfig != null)? borderConfig.getRows() : List.of(1, 6);
    for(final int row : borderRows) {
      menuPage.setRow(row, borderBuilder);
    }

    // === Control Row (Row 1) ===

    // Search button (slot 0) - Left-click to search, Right-click to clear
    final String searchMaterial = (searchConfig != null)? searchConfig.getMaterial() : "ANVIL";
    final int searchSlot = (searchConfig != null)? searchConfig.getSlot() : 0;
    final String currentSearchDisplay = searchQuery.isEmpty()? "None" : searchQuery;

    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(searchMaterial, 1)
                                             .customName(getConfigDisplay(id, searchConfig, "<yellow>Search: {0}</yellow>", currentSearchDisplay))
                                             .lore(getConfigLore(id, searchConfig, currentSearchDisplay)))
                             .withSlot(searchSlot)
                             .withActions(new GuiChatAction((message)->{
                               // Handle clear command
                               final String searchValue = (message.equalsIgnoreCase("clear") || message.equals("0"))? "" : message;

                               // Create new viewer with ALL state preserved + new search value
                               // (TNMS removes viewer when inventory closes, so we recreate it)
                               final MenuViewer newViewer = new MenuViewer(id);
                               newViewer.addData(SHOPS_DATA, allShops);  // Captured from closure
                               newViewer.addData(BROWSE_SORT, sortMode);   // Captured from closure
                               newViewer.addData(BROWSE_FILTER, filterMode); // Captured from closure
                               newViewer.addData(BROWSE_STOCK_ONLY, stockOnly); // Captured from closure
                               newViewer.addData(BROWSE_SEARCH, searchValue); // New search value
                               newViewer.addData(SHOPS_PAGE, 1); // Reset to page 1 on new search
                               MenuManager.instance().addViewer(newViewer);

                               // Manually reopen the menu
                               final Player p = Bukkit.getPlayer(id);
                               if(p != null && p.isOnline()) {
                                 final MenuPlayer menuPlayer = QuickShop.getInstance().createMenuPlayer(p);
                                 menuPlayer.inventory().openMenu(menuPlayer, menuName, 1);  // Page 1 is GroupedItemPage
                               }
                               return true;
                             }, guiMessage("browse.enter-search"), false, ActionType.LEFT_CLICK))  // Left-click for search input
                             .withActions(new ClearSearchAction(BROWSE_SEARCH, SHOPS_PAGE, menuName, 1))  // Right-click to clear
                             .build());

    // Sort button (slot 2)
    final String sortMaterial = (sortConfig != null)? sortConfig.getMaterial() : "HOPPER";
    final int sortSlot = (sortConfig != null)? sortConfig.getSlot() : 2;

    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(sortMaterial, 1)
                                             .customName(getConfigDisplay(id, sortConfig, "<green>Sort: {0}</green>", getSortDisplayName(id, sortMode)))
                                             .lore(getConfigLore(id, sortConfig)))
                             .withSlot(sortSlot)
                             .withActions(
                                     new DataAction(BROWSE_SORT, sortMode.next()),
                                     new DataAction(SHOPS_PAGE, 1),
                                     new SwitchPageAction(menuName, 1)
                                         )
                             .build());

    // Filter button (slot 4)
    final String filterMaterial = (filterConfig != null)? filterConfig.getMaterial() : "NAME_TAG";
    final int filterSlot = (filterConfig != null)? filterConfig.getSlot() : 4;

    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(filterMaterial, 1)
                                             .customName(getConfigDisplay(id, filterConfig, "<aqua>Filter: {0}</aqua>", getFilterDisplayName(id, filterMode)))
                                             .lore(getConfigLore(id, filterConfig)))
                             .withSlot(filterSlot)
                             .withActions(
                                     new DataAction(BROWSE_FILTER, filterMode.next()),
                                     new DataAction(SHOPS_PAGE, 1),
                                     new SwitchPageAction(menuName, 1)
                                         )
                             .build());

    // Stock filter toggle button (slot 6)
    final String stockMaterial = (stockConfig != null)? stockConfig.getMaterial() : "CHEST";
    final int stockSlot = (stockConfig != null)? stockConfig.getSlot() : 6;
    final String stockStatus = (stockOnly)? "ON" : "OFF";

    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(stockMaterial, 1)
                                             .customName(getConfigDisplay(id, stockConfig, "<gold>In Stock Only: {0}</gold>", stockStatus))
                                             .lore(getConfigLore(id, stockConfig)))
                             .withSlot(stockSlot)
                             .withActions(
                                     new DataAction(BROWSE_STOCK_ONLY, !stockOnly),
                                     new DataAction(SHOPS_PAGE, 1),
                                     new SwitchPageAction(menuName, 1)
                                         )
                             .build());

    // Close button (slot 8)
    final String closeMaterial = (closeConfig != null)? closeConfig.getMaterial() : "BARRIER";
    final int closeSlot = (closeConfig != null)? closeConfig.getSlot() : 8;

    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(closeMaterial, 1)
                                             .customName(getConfigDisplay(id, closeConfig, "<red>Close</red>")))
                             .withSlot(closeSlot)
                             .withActions(new RunnableAction((click)->{
                               final Player p = Bukkit.getPlayer(click.player().identifier());
                               if(p != null) p.closeInventory();
                             }))
                             .build());

    //  Pagination Row (Bottom) 
    final String prevMaterial = (prevPageConfig != null)? prevPageConfig.getMaterial() : "ARROW";
    final int prevSlot = (prevPageConfig != null)? prevPageConfig.getSlot() : 48;
    final String nextMaterial = (nextPageConfig != null)? nextPageConfig.getMaterial() : "ARROW";
    final int nextSlot = (nextPageConfig != null)? nextPageConfig.getSlot() : 50;
    final String pageInfoMaterial = (pageInfoConfig != null)? pageInfoConfig.getMaterial() : "BOOK";
    final int pageInfoSlot = (pageInfoConfig != null)? pageInfoConfig.getSlot() : 49;

    if(maxPages > 1) {
      menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(prevMaterial, 1)
                                               .customName(getConfigDisplay(id, prevPageConfig, "<white><< Previous Page</white>")))
                               .withSlot(prevSlot)
                               .withActions(new DataAction(SHOPS_PAGE, prev), new SwitchPageAction(menuName, 1))
                               .build());

      menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(nextMaterial, 1)
                                               .customName(getConfigDisplay(id, nextPageConfig, "<white>Next Page >></white>")))
                               .withSlot(nextSlot)
                               .withActions(new DataAction(SHOPS_PAGE, next), new SwitchPageAction(menuName, 1))
                               .build());
    }

    // Page info
    menuPage.addIcon(new IconBuilder(QuickShop.getInstance().stack().of(pageInfoMaterial, 1)
                                             .customName(getConfigDisplay(id, pageInfoConfig, "<yellow>Page {0}/{1}</yellow>", page, Math.max(1, maxPages))))
                             .withSlot(pageInfoSlot)
                             .build());

    // === Item Grid ===
    int i = 0;
    for(final MarketItemGroup group : groups) {
      if(i < start) {
        i++;
        continue;
      }
      if(i >= (start + items)) break;

      // Build item lore with market statistics
      final List<Component> lore = buildGroupLore(id, player.getWorld().getName(), group);

      // Get display name for the item
      final String itemName = CommonUtil.prettifyText(group.getRepresentativeItem().getType().name());

      final AbstractItemStack<ItemStack> stack = new BukkitItemStack()
              .of(group.getRepresentativeItem().getType().key().asString(), 1)
              .customName(getConfigDisplay(id, groupedConfig, "<yellow>{0}</yellow>", itemName))
              .lore(lore);

      // Find ALL shops for this item type from unfiltered list (so filter can be changed on ShopListPage)
      final ItemStack representativeItem = group.getRepresentativeItem();
      final List<Shop> allShopsForItem = allShops.stream()
              .filter(s->s.getItem().getType() == representativeItem.getType())
              .toList();

      menuPage.addIcon(new IconBuilder(stack)
                               .withSlot(listStartSlot + (i - start))
                               .withActions(
                                       new RunnableAction((click)->{
                                         final Optional<MenuViewer> v = click.player().viewer();
                                         if(v.isPresent()) {
                                           v.get().addData(SELECTED_ITEM_SHOPS, new ArrayList<>(allShopsForItem));
                                           v.get().addData(SHOP_LIST_PAGE, 1);
                                         }
                                       }),
                                       new SwitchPageAction(menuName, 2)
                                           )
                               .build());

      i++;
    }
  }

  private String getSortDisplayName(final UUID id, final BrowseSortMode mode) {

    return switch(mode) {
      case PRICE_ASC -> QuickShop.getInstance().text().of(id, "gui.browse.sort.price-asc").legacy();
      case PRICE_DESC -> QuickShop.getInstance().text().of(id, "gui.browse.sort.price-desc").legacy();
      case STOCK -> QuickShop.getInstance().text().of(id, "gui.browse.sort.stock").legacy();
      case NAME -> QuickShop.getInstance().text().of(id, "gui.browse.sort.name").legacy();
    };
  }

  private String getFilterDisplayName(final UUID id, final BrowseFilterMode mode) {

    return switch(mode) {
      case ALL -> QuickShop.getInstance().text().of(id, "gui.browse.filter.all").legacy();
      case BUYING -> QuickShop.getInstance().text().of(id, "gui.browse.filter.buying").legacy();
      case SELLING -> QuickShop.getInstance().text().of(id, "gui.browse.filter.selling").legacy();
    };
  }

  /**
   * Build the lore for a grouped item showing market statistics
   */
  private List<Component> buildGroupLore(final UUID id, final String world, final MarketItemGroup group) {

    final var text = QuickShop.getInstance().text();

    //initialize with our shops count.
    final List<Component> lore = new ArrayList<>(text.ofList(id, "gui.browse.grouped-item.lore-title", group.getTotalShopCount()).forLocale());
    lore.add(Component.empty());

    //our selling statistics.
    if(group.hasSellingShops()) {
      lore.addAll(text.ofList(id, "gui.browse.grouped-item.lore-selling", group.getSellingShopCount(),
                              formatPrice(world, group.getSellingMinPrice()),
                              formatPrice(world, group.getSellingMaxPrice()),
                              formatPrice(world, group.getSellingAvgPrice()),
                              formatPrice(world, group.getSellingMedianPrice()),
                              group.getSellingTotalStock()).forLocale());
    }

    if(group.hasBuyingShops()) {

      if(group.hasSellingShops()) {
        lore.add(Component.empty());
      }

      lore.addAll(text.ofList(id, "gui.browse.grouped-item.lore-buying", group.getBuyingShopCount(),
                              formatPrice(world, group.getBuyingMinPrice()),
                              formatPrice(world, group.getBuyingMaxPrice()),
                              formatPrice(world, group.getBuyingAvgPrice()),
                              formatPrice(world, group.getBuyingMedianPrice())).forLocale());

      lore.add(Component.empty());
      lore.addAll(text.ofList(id, "gui.browse.grouped-item.lore-footer").forLocale());
    }

    return lore;
  }

  /**
   * Format a price value
   */
  private String formatPrice(final String world, final double price) {

    if(QuickShop.getInstance().getEconomyManager().provider() == null) {
      return String.valueOf(price);
    }
    return QuickShop.getInstance().getEconomyManager().provider()
            .format(BigDecimal.valueOf(price), world, null);
  }
}
