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
import com.ghostchu.quickshop.api.economy.AbstractEconomy;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import net.tnemc.item.AbstractItemStack;
import net.tnemc.item.bukkit.BukkitItemStack;
import net.tnemc.item.providers.SkullProfile;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.IconAction;
import net.tnemc.menu.core.icon.action.impl.DataAction;
import net.tnemc.menu.core.icon.action.impl.SwitchPageAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SHOPS_DATA;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.get;
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

  public MainPage(String returnMenu, String menuName,
                  final int menuPage, final int returnPage, String staffPageID,
                  final int menuRows, String iconLore, final IconAction... actions) {
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
    if(viewer.isPresent()) {

      final Optional<Object> shopsData = viewer.get().findData(SHOPS_DATA);
      final Player player = Bukkit.getPlayer(viewer.get().uuid());
      if(shopsData.isPresent() && player != null) {

        callback.getPage().getIcons().clear();
        final UUID id = viewer.get().uuid();

        final int offset = 9;
        final int page = (Integer)viewer.get().dataOrDefault(staffPageID, 1);
        final int items = (menuRows - 2) * offset;
        final int start = ((page - 1) * offset);

        final List<Shop> shops = (ArrayList<Shop>)shopsData.get();

        final int maxPages = (shops.size() / items) + (((shops.size() % items) > 0)? 1 : 0);

        final int prev = (page <= 1)? maxPages : page - 1;
        final int next = (page >= maxPages)? 1 : page + 1;
        final IconBuilder borderBuilder = new IconBuilder(QuickShop.getInstance().stack().of("WHITE_STAINED_GLASS_PANE", 1));
        callback.getPage().setRow(1, borderBuilder);
        callback.getPage().setRow(menuRows, borderBuilder);

        if(maxPages > 1) {

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("RED_WOOL", 1)
                  .display(get(id, "gui.shared.previous-page"))
                  .lore(List.of(get(id, "history.shop.current-page", page))))
                  .withActions(new DataAction(staffPageID, prev), new SwitchPageAction(menuName, menuPage))
                  .withSlot(3)
                  .build());

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("GREEN_WOOL", 1)
                  .display(get(id, "gui.shared.next-page"))
                  .lore(List.of(get(id, "history.shop.current-page", page))))
                  .withActions(new DataAction(staffPageID, next), new SwitchPageAction(menuName, menuPage))
                  .withSlot(5)
                  .build());
        }

        callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("BOOK", 1)
                .display(get(id, "history.shop.current-page", page)))
                .withSlot(4)
                .build());

        int i = 0;
        for(final Shop shop : shops) {

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

          final AbstractEconomy eco = QuickShop.getInstance().getEconomy();
          final AbstractItemStack<ItemStack> stack = new BukkitItemStack().of(shop.getItem().getType().getKey().toString(), shop.getShopStackingAmount())
                  .lore(getList(id, iconLore,
                  shop.getOwner().getDisplay(),
                  location,
                  shop.getShopType(),
                  eco.format(shop.getPrice(), shop.getLocation().getWorld(), shop.getCurrency())));

          callback.getPage().addIcon(new IconBuilder(stack).withSlot(offset + (i - start)).build());

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