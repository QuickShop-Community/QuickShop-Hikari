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
import com.ghostchu.quickshop.api.economy.EconomyProvider;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.menu.config.GuiConfig;
import net.kyori.adventure.text.Component;
import net.tnemc.item.AbstractItemStack;
import net.tnemc.item.bukkit.BukkitItemStack;
import net.tnemc.item.providers.SkullProfile;
import net.tnemc.menu.core.PlayerInstancePage;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.IconAction;
import net.tnemc.menu.core.icon.action.impl.DataAction;
import net.tnemc.menu.core.icon.action.impl.SwitchPageAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SHOPS_DATA;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.get;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigDisplay;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigLore;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getList;

/**
 * MainPage
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class MainPage {

  protected final String returnMenu;
  protected final String menuName;
  protected final int menuPage;
  protected final int returnPage;
  protected final String staffPageID;
  protected final int menuRows;
  protected final String iconLore;
  protected final IconAction[] actions;

  public MainPage(final String returnMenu, final String menuName,
                  final int menuPage, final int returnPage, final String staffPageID,
                  final int menuRows, final String iconLore, final IconAction... actions) {

    this.returnMenu = returnMenu;
    this.menuName = menuName;
    this.menuPage = menuPage;
    this.returnPage = returnPage;
    this.staffPageID = staffPageID;
    this.iconLore = iconLore;
    this.actions = actions;

    //we need a controller row and then at least one row for items.
    this.menuRows = (menuRows <= 1)? 3 : menuRows;
  }

  public void handle(final PageOpenCallback callback) {

    final Optional<MenuViewer> viewer = callback.getPlayer().viewer();
    if(viewer.isPresent() && callback.getPage() instanceof final PlayerInstancePage playerPage) {

      final Optional<Object> shopsData = viewer.get().findData(SHOPS_DATA);
      final UUID id = viewer.get().uuid();
      final Player player = Bukkit.getPlayer(id);
      if(shopsData.isPresent() && player != null) {

        playerPage.getIcons(id).clear();

        // Load GUI configuration
        final GuiConfig.MenuConfig menuConfig = QuickShop.getInstance().getGuiConfig().getMenuConfig("browse");
        final GuiConfig.IconConfig borderConfig = menuConfig != null ? menuConfig.getIcon("border") : null;
        final GuiConfig.IconConfig prevPageConfig = menuConfig != null ? menuConfig.getIcon("previous-page") : null;
        final GuiConfig.IconConfig nextPageConfig = menuConfig != null ? menuConfig.getIcon("next-page") : null;
        final GuiConfig.IconConfig pageInfoConfig = menuConfig != null ? menuConfig.getIcon("page-info") : null;

        final int listStartSlot = menuConfig != null ? menuConfig.getSection().getInt("list-start-slot", 9) : 9;

        final int offset = 9;
        final int page = (Integer)viewer.get().dataOrDefault(staffPageID, 1);
        final int items = (menuRows - 2) * offset;
        final int start = ((page - 1) * offset);

        final List<Shop> shops = (ArrayList<Shop>)shopsData.get();

        final int maxPages = (shops.size() / items) + (((shops.size() % items) > 0)? 1 : 0);

        final int prev = (page <= 1)? maxPages : page - 1;
        final int next = (page >= maxPages)? 1 : page + 1;

        // Set up borders from config
        final String borderMaterial = borderConfig != null ? borderConfig.getMaterial() : "GRAY_STAINED_GLASS_PANE";
        final IconBuilder borderBuilder = new IconBuilder(QuickShop.getInstance().stack().of(borderMaterial, 1));
        final List<Integer> borderRows = borderConfig != null ? borderConfig.getRows() : List.of(1, 6);
        for (final int row : borderRows) {
          playerPage.setRow(id, row, borderBuilder);
        }

        // Pagination icons from config
        final String prevPageMaterial = prevPageConfig != null ? prevPageConfig.getMaterial() : "ARROW";
        final int prevPageSlot = prevPageConfig != null ? prevPageConfig.getSlot() : 3;
        final String nextPageMaterial = nextPageConfig != null ? nextPageConfig.getMaterial() : "ARROW";
        final int nextPageSlot = nextPageConfig != null ? nextPageConfig.getSlot() : 5;
        final String pageInfoMaterial = pageInfoConfig != null ? pageInfoConfig.getMaterial() : "BOOK";
        final int pageInfoSlot = pageInfoConfig != null ? pageInfoConfig.getSlot() : 4;

        if(maxPages > 1) {

          playerPage.addIcon(id, new IconBuilder(QuickShop.getInstance().stack().of(prevPageMaterial, 1)
                                                         .display(getConfigDisplay(prevPageConfig, "<white><< Previous Page</white>"))
                                                         .lore(getConfigLore(prevPageConfig, page)))
                  .withActions(new DataAction(staffPageID, prev), new SwitchPageAction(menuName, menuPage))
                  .withSlot(prevPageSlot)
                  .build());

          playerPage.addIcon(id, new IconBuilder(QuickShop.getInstance().stack().of(nextPageMaterial, 1)
                                                         .display(getConfigDisplay(nextPageConfig, "<white>Next Page >></white>"))
                                                         .lore(getConfigLore(nextPageConfig, page)))
                  .withActions(new DataAction(staffPageID, next), new SwitchPageAction(menuName, menuPage))
                  .withSlot(nextPageSlot)
                  .build());
        }

        playerPage.addIcon(id, new IconBuilder(QuickShop.getInstance().stack().of(pageInfoMaterial, 1)
                                                       .display(getConfigDisplay(pageInfoConfig, "<yellow>Page {0}</yellow>", page)))
                .withSlot(pageInfoSlot)
                .build());

        int i = 0;
        for(final Shop shop : shops) {

          //System.out.println("Menu add: id: " + shop.getShopId() + " slot: " + offset + (i - start) + "i: " + i);

          if(i < start) {

            i++;

            continue;
          }

          if(i >= (start + items)) break;

          final String world = (shop.getLocation().getWorld() != null)? shop.getLocation().getWorld().getName() : "World";
          final String location = world + " " + shop.getLocation().getBlockX() + ", " + shop.getLocation().getBlockY() + ", " + shop.getLocation().getBlockZ();
          final QUser owner = shop.getOwner();
          SkullProfile ownerProfile = null;
          if(owner.isRealPlayer() && owner.getUniqueId() != null) {

            ownerProfile = new SkullProfile();
            ownerProfile.setUuid(owner.getUniqueId());
          }

          final EconomyProvider eco = QuickShop.getInstance().getEconomyManager().provider();
          final String priceFormatted = eco.format(BigDecimal.valueOf(shop.getPrice()), shop.getLocation().getWorld().getName(), shop.getCurrency());
          
          // Build lore dynamically for shop info
          final List<Component> shopLore = new ArrayList<>();
          shopLore.add(QuickShop.getInstance().platform().miniMessage().deserialize("<gray>Owner: <white>" + shop.getOwner().getDisplay() + "</white></gray>"));
          shopLore.add(QuickShop.getInstance().platform().miniMessage().deserialize("<gray>Location: <white>" + location + "</white></gray>"));
          shopLore.add(QuickShop.getInstance().platform().miniMessage().deserialize("<gray>Type: <white>" + shop.shopType().identifier() + "</white></gray>"));
          shopLore.add(QuickShop.getInstance().platform().miniMessage().deserialize("<gray>Price: <white>" + priceFormatted + "</white></gray>"));
          shopLore.add(QuickShop.getInstance().platform().miniMessage().deserialize("<gray>Stock: <white>" + MarketUtils.getStockFromCache(shop) + "</white></gray>"));
          
          final AbstractItemStack<ItemStack> stack = new BukkitItemStack().of(shop.getItem().getType().key().asString(), shop.getShopStackingAmount())
                  .lore(shopLore);

          playerPage.addIcon(id, new IconBuilder(stack).withSlot(listStartSlot + (i - start)).build());

          //System.out.println("Slots: " + playerPage.getIcons(id).size());
          i++;
        }
      }
    }
  }

  private Component hours(final UUID id, final int hours) {

    return get(id, "timeunit.hours", hours);
  }

  private Component days(final UUID id, final int days) {

    return get(id, "timeunit.days", days);
  }
}