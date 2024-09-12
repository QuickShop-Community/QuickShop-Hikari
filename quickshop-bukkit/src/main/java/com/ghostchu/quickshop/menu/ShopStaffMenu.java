package com.ghostchu.quickshop.menu;
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

import com.ghostchu.quickshop.menu.shared.QuickShopMenu;
import com.ghostchu.quickshop.menu.staff.PlayerSelectionPage;
import com.ghostchu.quickshop.menu.staff.StaffSelectionPage;
import net.tnemc.menu.core.Page;

import static com.ghostchu.quickshop.menu.ShopKeeperMenu.KEEPER_MAIN;

/**
 * ShopStaffMenu
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class ShopStaffMenu extends QuickShopMenu {

  public static final int STAFF_MAIN = 1;
  public static final int STAFF_ADD = 2;

  public static final String STAFF_PAGE = "STAFF_SELECTION_PAGE";
  public static final String PLAYER_PAGE = "PAGE_SELECTION_PAGE";


  public ShopStaffMenu() {

    this.rows = 6;
    this.name = "qs:staff";
    this.title = "Shop Keeper";

    setOpen((open)->open.getMenu().setTitle(legacy(open.getPlayer().identifier(), "gui.keeper.title")));

    final Page main = new Page(STAFF_MAIN);
    final StaffSelectionPage staffSelection = new StaffSelectionPage("qs:keeper", this.name, STAFF_MAIN, KEEPER_MAIN, STAFF_PAGE, this.rows, "gui.staff.head-icon.lore");
    main.setOpen(staffSelection::handle);

    final Page add = new Page(STAFF_ADD);
    final PlayerSelectionPage playerSelection = new PlayerSelectionPage(this.name, this.name, STAFF_ADD, STAFF_MAIN, PLAYER_PAGE, this.rows, "gui.player.select");
    add.setOpen(playerSelection::handle);

    addPage(main);
    addPage(add);
  }
}